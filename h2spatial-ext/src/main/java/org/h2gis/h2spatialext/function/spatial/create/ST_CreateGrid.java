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
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
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
import org.h2gis.utilities.SFSUtilities;

/**
 * Create a regular grid based on a table.
 *
 * @author Erwan Bocher
 */
public class ST_CreateGrid extends AbstractFunction implements ScalarFunction {

    private static final GeometryFactory GF = new GeometryFactory();

    public ST_CreateGrid() {
        addProperty(PROP_REMARKS, "Calculate a regular grid.\n"
                + "The first argument could be a geometry or a table name.\n"
                + "The delta X and Y cell grid are expressed in a cartesian plan.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGrid";
    }
    
    /**
     * Create a regular grid using the first geometry to compute the full
     * extent.
     *
     * @param connection
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @return a resultset that contains all cells as a set of polygons
     * @throws SQLException
     */
    public static ResultSet createGrid(Connection connection, Value value, int deltaX, int deltaY) throws SQLException {
        if(value instanceof ValueString){
            return createGridFromTable(connection, value.getString(), deltaX, deltaY);
        }
        else if (value instanceof ValueGeometry){
            ValueGeometry geom = (ValueGeometry) value;
            return computeGrid(geom.getGeometry().getEnvelopeInternal(), deltaX, deltaY);
        }
        else{
            throw new SQLException("This function supports only table name or geometry as first argument.");
        }
    }
    
        /**
     * Create a regular grid using the first geometry to compute the full
     * extent.
     *
     * @param connection
     * @param tableName the name of the table
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @return a resultset that contains all cells as a set of polygons
     * @throws SQLException
     */
    public static ResultSet createGridFromTable(Connection connection, String tableName, int deltaX, int deltaY) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("select ST_Extent(" + getGeometryField(tableName, connection) + ")  from " + tableName);
        rs.next();
        Polygon env = (Polygon) rs.getObject(1);
        if (env == null) {
            return null;
        }
        return computeGrid(env.getEnvelopeInternal(), deltaX, deltaY);
    }

    /**
     * Return the final grid based on envelope, deltaX and deltaY
     *
     * @param envelope
     * @param deltaX
     * @param deltaY
     * @return
     */
    private static ResultSet computeGrid(Envelope envelope, int deltaX, int deltaY) {
        SimpleResultSet srs = new SimpleResultSet(new GridRowSet(envelope, deltaX, deltaY));
        srs.addColumn("THE_GEOM", Types.JAVA_OBJECT, "GEOMETRY", 0, 0);
        srs.addColumn("ID", Types.INTEGER, 10, 0);
        srs.addColumn("ID_COL", Types.INTEGER, 10, 0);
        srs.addColumn("ID_ROW", Types.INTEGER, 10, 0);
        return srs;
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
    public static String getGeometryField(String tableName, Connection connection) throws SQLException {
        // Find first geometry column
        List<String> geomFields = SFSUtilities.getGeometryFields(connection, SFSUtilities.splitCatalogSchemaTableName(tableName));
        if (!geomFields.isEmpty()) {
            return geomFields.get(0);
        } else {
            throw new SQLException("The table " + tableName + " does not contain a geometry field");
        }
    }

    /**
     * GridRowSet is used to populate the result table with all grid cell
     */
    private static class GridRowSet implements SimpleRowSource {

        private int cellI = 0;
        private int cellJ = 0;
        private double cellWidth, cellHeight;
        private final int maxI, maxJ;
        private final double minX, minY;
        int id = 0;

        private GridRowSet(Envelope envelope, int deltaX, int deltaY) {
            minX = envelope.getMinX();
            minY = envelope.getMinY();
            cellWidth = envelope.getWidth();
            cellHeight = envelope.getHeight();
            maxI = (int) Math.ceil(cellWidth
                    / deltaX);
            maxJ = (int) Math.ceil(cellHeight
                    / deltaY);
        }

        @Override
        public Object[] readRow() throws SQLException {
            if (cellI > maxI) {
                cellJ++;
                cellI = 0;
                return null;
            } else if (cellJ > maxJ) {
                cellJ++;
                cellI = 0;
                return null;
            }
            return new Object[]{getCellEnv(minX, minY, cellI++, cellJ++, cellWidth, cellHeight), id++, cellI, cellJ};
        }

        @Override
        public void close() {
        }

        @Override
        public void reset() throws SQLException {
            cellI = 0;
            cellJ = 0;
        }
    }

    /**
     * Compute the envelope corresponding to parameters
     *
     * @param mainEnvelope Global envelope
     * @param cellI I cell index
     * @param cellJ J cell index
     * @param cellWidth Cell width meter
     * @param cellHeight Cell height meter
     * @return Envelope of the cell
     */
    public static Polygon getCellEnv(double minX, double minY, int cellI, int cellJ, double cellWidth, double cellHeight) {
        final Coordinate[] summits = new Coordinate[5];
        double x1 = minX + cellI * cellWidth;
        double y1 = minY + cellHeight * cellJ;
        double x2 = minX + cellI * cellWidth + cellWidth;
        double y2 = minY + cellHeight * cellJ + cellHeight;
        summits[0] = new Coordinate(x1, y1);
        summits[1] = new Coordinate(x2, y1);
        summits[2] = new Coordinate(x2, y2);
        summits[3] = new Coordinate(x1, y2);
        summits[4] = new Coordinate(x1, y1);
        final LinearRing g = GF.createLinearRing(summits);
        final Polygon gg = GF.createPolygon(g, null);
        return gg;
    }
}
