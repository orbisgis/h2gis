/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueString;
import org.h2gis.h2spatialapi.AbstractFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;
import org.h2gis.h2spatialapi.ScalarFunction;
import static org.h2gis.h2spatialapi.ScalarFunction.PROP_NOCACHE;
import org.h2gis.utilities.SFSUtilities;

/**
 * Create a regular grid of points based on a table or a geometry envelope.
 *
 * @author Erwan Bocher
 */
public class ST_MakeGridPoints extends AbstractFunction implements ScalarFunction {

    private static final GeometryFactory GF = new GeometryFactory();

    public ST_MakeGridPoints() {
        addProperty(PROP_REMARKS, "Calculate a regular grid of points.\n"
                + "The first argument could be a geometry or a table name.\n"
                + "The delta X and Y cell grid are expressed in a cartesian plan.");
        addProperty(PROP_NOCACHE, true);
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGridPoints";
    }

    /**
     * Create a regular grid of points using the first input value to compute
     * the full extent.
     *
     * @param connection
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @return a resultset that contains all cells as a set of polygons
     * @throws SQLException
     */
    public static ResultSet createGridPoints(Connection connection, Value value, int deltaX, int deltaY) throws SQLException {
        if (value instanceof ValueString) {
            GridPointsRowSet gridRowSet = new GridPointsRowSet(connection, deltaX, deltaY, value.getString());
            return gridRowSet.getResultSet();
        } else if (value instanceof ValueGeometry) {
            ValueGeometry geom = (ValueGeometry) value;
            GridPointsRowSet gridRowSet = new GridPointsRowSet(connection, deltaX, deltaY, geom.getGeometry().getEnvelopeInternal());
            return gridRowSet.getResultSet();
        } else {
            throw new SQLException("This function supports only table name or geometry as first argument.");
        }
    }

    /**
     * Return the first spatial geometry field name
     *
     * @param tableName
     * @param spatialFieldName
     * @param connection
     * @return
     * @throws SQLException
     */
    public static String getFirstGeometryField(String tableName, Connection connection) throws SQLException {
        // Find first geometry column
        List<String> geomFields = SFSUtilities.getGeometryFields(connection, SFSUtilities.splitCatalogSchemaTableName(tableName));
        if (!geomFields.isEmpty()) {
            return geomFields.get(0);
        } else {
            throw new SQLException("The table " + tableName + " does not contain a geometry field");
        }
    }

    /**
     * GridPointsRowSet is used to populate the result table with all points
     * cell
     */
    private static class GridPointsRowSet implements SimpleRowSource {

        private static int cellI = 0;
        private static int cellJ = 0;
        private int maxI, maxJ, deltaX, deltaY;
        private double minX, minY;
        int id = 0;
        private final Connection connection;
        boolean firstRow = true;
        private Envelope envelope;
        private boolean isTable;
        private String tableName;
        private double cellWidth;
        private double cellHeight;

        private GridPointsRowSet(Connection connection, int deltaX, int deltaY, String tableName) {
            this.connection = connection;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.tableName = tableName;
            this.isTable = true;
        }

        private GridPointsRowSet(Connection connection, int deltaX, int deltaY, Envelope envelope) {
            this.connection = connection;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.envelope = envelope;
            this.isTable = false;
        }

        @Override
        public Object[] readRow() throws SQLException {
            if (firstRow) {
                reset();
            }
            if (cellI == maxI) {
                cellJ++;
                cellI = 0;
            }
            if (cellJ >= maxJ) {
                cellJ = 0;
                return null;
            }
            return new Object[]{getCellEnv(), id++, cellI, cellJ};
        }

        @Override
        public void close() {
        }

        @Override
        public void reset() throws SQLException {
            cellI = 0;
            cellJ = 0;
            firstRow = false;
            //We compute the extend according the first input value
            if (isTable) {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("select ST_Extent(" + getFirstGeometryField(tableName, connection) + ")  from " + tableName);
                rs.next();
                Geometry geomExtend = (Geometry) rs.getObject(1);
                if (geomExtend == null) {
                    throw new SQLException("The envelope cannot be null.");
                } else {
                    envelope = geomExtend.getEnvelopeInternal();
                    initParameters();
                }
            } else {
                if (envelope == null) {
                    throw new SQLException("The input geometry used to compute the grid cannot be null.");
                } else {
                    initParameters();
                }
            }
        }

        /**
         * Compute the parameters need to create each cells
         *
         */
        public void initParameters() {
            this.minX = envelope.getMinX();
            this.minY = envelope.getMinY();
            this.cellWidth = envelope.getWidth();
            this.cellHeight = envelope.getHeight();
            this.maxI = (int) Math.ceil(cellWidth
                    / deltaX);
            this.maxJ = (int) Math.ceil(cellHeight
                    / deltaY);
        }

        /**
         * Compute the point of the cell
         *
         * @return Center point of the cell
         */
        public Point getCellEnv() {
            double x1 = (minX + cellI * deltaX)+(deltaX/2d);
            double y1 = (minY + cellJ * deltaY)+(deltaY/2d);
            cellI++;
            return GF.createPoint(new Coordinate(x1, y1));
        }

        /**
         * Give the regular grid
         *
         * @return ResultSet
         * @throws SQLException
         */
        public ResultSet getResultSet() throws SQLException {
            SimpleResultSet srs = new SimpleResultSet(this);
            srs.addColumn("THE_GEOM", Types.JAVA_OBJECT, "GEOMETRY", 0, 0);
            srs.addColumn("ID", Types.INTEGER, 10, 0);
            srs.addColumn("ID_COL", Types.INTEGER, 10, 0);
            srs.addColumn("ID_ROW", Types.INTEGER, 10, 0);
            return srs;
        }
    }
}
