/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.topography;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2.value.Value;
import org.h2.value.ValueString;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.jts_utils.Contouring;
import org.h2gis.utilities.jts_utils.TriMarkers;

import java.sql.*;
import java.util.*;
import org.h2.value.ValueArray;

/**
 * Split triangle into area within the specified range values.
 * *********************************
 * ANR EvalPDU
 * IFSTTAR 11_05_2011
 *
 * @author Nicolas FORTIN
 * @author Judicaël PICAUT
 **********************************
 */
public class ST_TriangleContouring extends DeterministicScalarFunction {
    /** The default field name for explode count, value is [1-n] */
    public static final String ISO_FIELD_NAME = "IDISO";
    private static final String HACK_URL = "jdbc:columnlist:connection";

    public ST_TriangleContouring() {
        addProperty(PROP_REMARKS, "Split triangle into polygons within the specified range of values.\n" +
                "Iso contouring using Z:\n" +
                "select * from ST_TRIANGLECONTOURING('input_table',10,20,30,40)\n" +
                "Iso contouring using table columns\n" +
                "SELECT * FROM ST_TRIANGLECONTOURING('input_table','m1','m2','m3',10,20,30,40)");
        addProperty(PROP_NOBUFFER, true);
    }

    @Override
    public String getJavaStaticMethod() {
        return "triangleContouring";
    }
    
   
   

    /**
     * Iso contouring using Z,M attributes of geometries
     * @param connection Active connection
     * @param tableName Table name
     * @param varArgs Iso levels
     * @return Result Set
     * @throws SQLException
     */
    public static ResultSet triangleContouring(Connection connection, String tableName, Value... varArgs) throws SQLException {
        if (connection.getMetaData().getURL().equals(HACK_URL)) {
            return new ExplodeResultSet(connection,tableName, Arrays.asList(0.0)).getResultSet();
        }
        ExplodeResultSet rowSource = null;
        if(varArgs.length > 3) {
            // First ones may be column names
            if(varArgs[0] instanceof ValueString &&
                    varArgs[1] instanceof ValueString &&
                    varArgs[2] instanceof ValueString) {
                // Use table columns for iso levels
                List<Double> isoLvls = new ArrayList<Double>(varArgs.length - 3);
                for(int idArg = 3; idArg < varArgs.length; idArg++) {
                    isoLvls.add(varArgs[idArg].getDouble());
                }
                rowSource = new ExplodeResultSet(connection,tableName,varArgs[0].getString(), varArgs[1].getString(),
                        varArgs[2].getString(), isoLvls);
            }
        }
        if(rowSource == null) {
            // Use Z
            List<Double> isoLvls = new ArrayList<Double>(varArgs.length);
            for (Value value : varArgs) {
                if (value instanceof ValueArray) {
                    for (Value arrVal : ((ValueArray) value).getList()) {
                        isoLvls.add(arrVal.getDouble());
                    }
                } else {
                    isoLvls.add(value.getDouble());
                }
            }
            rowSource = new ExplodeResultSet(connection,tableName, isoLvls);
        }
        return rowSource.getResultSet();
    }

    /**
     * Explode fields only on request
     */
    private static class ExplodeResultSet implements SimpleRowSource {
        // If true, table query is closed the read again
        private boolean firstRow = true;
        private ResultSet tableQuery;
        private boolean endOfResultSet = false;
        private String tableName;
        private String spatialFieldName;
        private Integer spatialFieldIndex;
        private int columnCount;
        private Queue<GeneratedTriangle> generatedRows = new LinkedList<GeneratedTriangle>();
        private Connection connection;
        private boolean useZ;
        private String isoFieldName1 = "",isoFieldName2 = "",isoFieldName3 = "";
        /** Extract Z or Field value, depending on implementation */
        private TriMarkersFactory triFactory;
        private List<Double> isoLvls;
        private GeometryFactory factory = new GeometryFactory();

        private ExplodeResultSet(Connection connection, String tableName, String isoField1,String isoField2,String isoField3, List<Double> isoLvls) {
            this.tableName = tableName;
            this.spatialFieldName = "";
            this.connection = connection;
            useZ = false;
            this.isoFieldName1 = isoField1;
            this.isoFieldName2 = isoField2;
            this.isoFieldName3 = isoField3;
            this.isoLvls = isoLvls;
        }

        private ExplodeResultSet(Connection connection, String tableName, List<Double> isoLvls) {
            this.tableName = tableName;
            this.spatialFieldName = "";
            this.connection = connection;
            useZ = true;
            this.isoLvls = isoLvls;
        }

        @Override
        public Object[] readRow() throws SQLException {
            if(firstRow) {
                reset();
            }
            while(generatedRows.isEmpty() && !endOfResultSet) {
                parseRow();
            }
            if(generatedRows.isEmpty()) {
                // No more rows
                return null;
            } else {
                Object[] objects = new Object[columnCount+1];
                GeneratedTriangle result = generatedRows.remove();
                for(int i=1;i<=columnCount+1;i++) {
                    if(i==spatialFieldIndex) {
                        objects[i-1] = result.getTriangle();
                    } else if(i==columnCount+1) {
                        objects[i-1] = result.getIdIso();
                    } else {
                        objects[i-1] = tableQuery.getObject(i);
                    }
                }
                return objects;
            }
        }

        @Override
        public void close() {
            if(tableQuery!=null) {
                try {
                    tableQuery.close();
                    tableQuery = null;
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        private void parseRow() throws SQLException {
            generatedRows.clear();
            if(tableQuery.next()) {
                Geometry inputTriangle = (Geometry) tableQuery.getObject(spatialFieldIndex);
                if(inputTriangle == null || inputTriangle.getNumPoints() != 4) {
                    throw new SQLException("Invalid geometry input, got " + (inputTriangle == null ? "null" : inputTriangle.toText()));
                }
                // Compute ISO
                TriMarkers triangle = triFactory.getTriangle(inputTriangle.getCoordinates());
                Map<Short, Deque<TriMarkers>> result = Contouring.processTriangle(triangle, isoLvls);
                for(Map.Entry<Short, Deque<TriMarkers>> isoResult : result.entrySet()) {
                    for(TriMarkers outputTriangle : isoResult.getValue()) {
                        Coordinate[] pverts = {outputTriangle.p0, outputTriangle.p1, outputTriangle.p2,
                                outputTriangle.p0};
                        Polygon polygon = factory.createPolygon(factory.createLinearRing(pverts), null);
                        generatedRows.add(new GeneratedTriangle(polygon, isoResult.getKey()));
                    }
                }
            } else {
                endOfResultSet = true;
            }
        }

        @Override
        public void reset() throws SQLException {
            if(tableQuery!=null && !tableQuery.isClosed()) {
                close();
            }
            Statement st = connection.createStatement();
            tableQuery = st.executeQuery("SELECT * FROM "+tableName);
            firstRow = false;
            ResultSetMetaData meta = tableQuery.getMetaData();
            columnCount = meta.getColumnCount();
            if(spatialFieldName.isEmpty()) {
                // Find first geometry column
                List<String> geomFields = SFSUtilities.getGeometryFields(connection,TableLocation.parse(tableName));
                if(!geomFields.isEmpty()) {
                    spatialFieldName = geomFields.get(0);
                    spatialFieldIndex = tableQuery.findColumn(SFSUtilities.getGeometryFields(tableQuery).get(0));
                } else {
                    throw new SQLException("The table "+tableName+" does not contain a geometry field");
                }
            }

            if(useZ) {
                triFactory = new ValueOnZ();
            } else {
                int vertex1FieldIndex = tableQuery.findColumn(isoFieldName1);
                int vertex2FieldIndex = tableQuery.findColumn(isoFieldName2);
                int vertex3FieldIndex = tableQuery.findColumn(isoFieldName3);
                triFactory = new ValueOnField(vertex1FieldIndex, vertex2FieldIndex, vertex3FieldIndex, tableQuery);
            }
            if(spatialFieldIndex == null) {
                throw new SQLException("Geometry field "+spatialFieldName+" of table "+tableName+" not found");
            }
        }

        public ResultSet getResultSet() throws SQLException {
            SimpleResultSet rs = new SimpleResultSet(this);
            // Feed with fields
            TableUtilities.copyFields(connection, rs, TableLocation.parse(tableName, JDBCUtilities.isH2DataBase(connection.getMetaData())));
            rs.addColumn(ISO_FIELD_NAME, Types.INTEGER,10,0);
            return rs;
        }
    }

    /**
     * Triangle to add in table
     */
    private static class GeneratedTriangle {
        private final Polygon triangle;
        private final int idIso;

        private GeneratedTriangle(Polygon triangle, int idIso) {
            this.triangle = triangle;
            this.idIso = idIso;
        }

        public Polygon getTriangle() {
            return triangle;
        }

        public int getIdIso() {
            return idIso;
        }
    }

    /**
     * Triangle factory
     */
    private interface TriMarkersFactory {
        TriMarkers getTriangle(Coordinate[] pts) throws SQLException;
    }

    /**
     * Read vertex level from the Z value
     */
    private static class ValueOnZ implements TriMarkersFactory {
        @Override
        public TriMarkers getTriangle(Coordinate[] pts) throws SQLException {
            return new TriMarkers(pts[0], pts[1],
                    pts[2], pts[0].z,
                    pts[1].z,
                    pts[2].z);
        }
    }

    /**
     * Read vertex level from another fields
     */
    private static class ValueOnField implements TriMarkersFactory {
        private final int vertex1FieldIndex;
        private final int vertex2FieldIndex;
        private final int vertex3FieldIndex;
        private final ResultSet rs;

        private ValueOnField(int vertex1FieldIndex, int vertex2FieldIndex, int vertex3FieldIndex, ResultSet rs) {
            this.vertex1FieldIndex = vertex1FieldIndex;
            this.vertex2FieldIndex = vertex2FieldIndex;
            this.vertex3FieldIndex = vertex3FieldIndex;
            this.rs = rs;
        }

        @Override
        public TriMarkers getTriangle(Coordinate[] pts) throws SQLException {
            return new TriMarkers(pts[0], pts[1],
                    pts[2], rs.getDouble(vertex1FieldIndex),
                    rs.getDouble(vertex2FieldIndex),
                    rs.getDouble(vertex3FieldIndex));
        }
    }
}
