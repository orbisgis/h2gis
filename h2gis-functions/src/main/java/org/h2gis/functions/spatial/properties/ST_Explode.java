/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.properties;

import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.*;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.h2gis.utilities.GeometryTableUtilities;

/**
 * This table function explode Geometry Collection into multiple geometries
 * @author Nicolas Fortin
 */
public class ST_Explode extends AbstractFunction implements ScalarFunction {
    /** The default field name for explode count, value is [1-n] */
    public static final String EXPLODE_FIELD = "EXPLOD_ID";

    public ST_Explode() {
        addProperty(PROP_REMARKS, "Explode Geometry Collection into multiple geometries.\n"
                + "Note : This function supports select query as the first argument.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "explode";
    }

    /**
     * Explode Geometry Collection into multiple geometries
     * @param connection database
     * @param tableName the name of the input table or select query
     * @return A result set with the same content of specified table but with atomic geometries and duplicate values.
     */
    public static ResultSet explode(Connection connection, String tableName) throws SQLException {
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableName);
        if (matcher.find()) {
            if (tableName.startsWith("(") && tableName.endsWith(")")) {
                ExplodeResultSetQuery explodeResultSetQuery = new ExplodeResultSetQuery(connection, tableName, null);
                return explodeResultSetQuery.getResultSet();
            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }
        }
        return explode(connection, tableName, null);
    }

    /**
     * Explode Geometry Collection into multiple geometries
     * @param connection database
     * @param tableName the name of the input table
     * @param fieldName the name of geometry field. If null the first geometry column is used.
     * @return ResultSet
     */
    public static ResultSet explode(Connection connection, String tableName, String fieldName) throws SQLException {
        ExplodeResultSet rowSource = new ExplodeResultSet(connection,
                TableLocation.parse(tableName, DBUtils.getDBType(connection)).toString(),fieldName);
        return rowSource.getResultSet();
    }

    /**
     * Explode fields only on request
     */
    public static class ExplodeResultSet implements SimpleRowSource {
        // If true, table query is closed the read again
        public boolean firstRow = true;
        public ResultSet tableQuery;
        public String tableName;
        public String spatialFieldName;
        public int spatialFieldIndex =-1;
        public int columnCount;
        public Queue<Geometry> sourceRowGeometries = new LinkedList<Geometry>();
        public int explodeId = 1;
        public Connection connection;
        private final TableLocation tableLocation;
        
        public ExplodeResultSet(Connection connection, String tableName, String spatialFieldName) throws SQLException {
            this.tableName = tableName;
            this.tableLocation=TableLocation.parse(tableName, DBUtils.getDBType(connection));
            this.spatialFieldName = spatialFieldName;
            this.connection = connection;
        }

        @Override
        public Object[] readRow() throws SQLException {
            if(firstRow) {
                reset();
            }
            if(sourceRowGeometries.isEmpty()) {
                parseRow();
            }
            if(sourceRowGeometries.isEmpty()) {
                // No more rows
                return null;
            } else {
                Object[] objects = new Object[columnCount+1];
                for(int i=1;i<=columnCount+1;i++) {
                    if(i==spatialFieldIndex) {
                        objects[i-1] = sourceRowGeometries.remove();
                    } else if(i==columnCount+1) {
                        objects[i-1] = explodeId++;
                    } else {
                        objects[i-1] = tableQuery.getObject(i);
                    }
                }
                return objects;
            }
        }
        
        /**
         * Explode the geometry
         * @param geometry 
         */
        private void explode(final Geometry geometry) {
            if (geometry instanceof GeometryCollection) {
                final int nbOfGeometries = geometry.getNumGeometries();
                for (int i = 0; i < nbOfGeometries; i++) {
                    explode(geometry.getGeometryN(i));
                }
            } else {
                sourceRowGeometries.add(geometry);
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

        /**
         * Read the geometry value and explode it.
         */
        private void parseRow() throws SQLException {
            sourceRowGeometries.clear();
            explodeId = 1;
            if(tableQuery.next()) {
                Geometry geometry = (Geometry) tableQuery.getObject(spatialFieldIndex);
                explode(geometry);
                // If the geometry is empty, set empty field or null if generic geometry collection
                if(sourceRowGeometries.isEmpty()) {
                    GeometryFactory factory = geometry.getFactory();
                    if(factory==null) {
                        factory = new GeometryFactory();
                    }
                    if(geometry instanceof MultiLineString) {
                        sourceRowGeometries.add(factory.createLineString(new Coordinate[0]));
                    } else if(geometry instanceof MultiPolygon) {
                        sourceRowGeometries.add((factory.createPolygon(null,null)));
                    } else {
                        sourceRowGeometries.add(null);
                    }
                }
            }
        }

        @Override
        public void reset() throws SQLException {
            if(tableQuery!=null && !tableQuery.isClosed()) {
                close();
            } 
            LinkedHashMap<String, Integer> geomNamesAndIndexes = GeometryTableUtilities.getGeometryColumnNamesAndIndexes(connection, tableLocation);
            Map.Entry<String, Integer> firstGeomNameAndIndex = geomNamesAndIndexes.entrySet().iterator().next();
            if (spatialFieldName != null && !spatialFieldName.isEmpty()) {
                Map.Entry<String, Integer> result = geomNamesAndIndexes.entrySet().stream()
                        .filter(tuple -> spatialFieldName.equalsIgnoreCase(tuple.getKey()))
                        .findAny()
                        .orElse(null);
                if (result != null) {
                    firstGeomNameAndIndex = result;
                }
            }            
            spatialFieldName = firstGeomNameAndIndex.getKey();
            spatialFieldIndex = firstGeomNameAndIndex.getValue();
            Statement st = connection.createStatement();
            tableQuery = st.executeQuery("SELECT * FROM "+tableLocation);
            firstRow = false;
            ResultSetMetaData meta = tableQuery.getMetaData();  
            columnCount = meta.getColumnCount();
           
        }

        /**
         * Return the exploded geometries as multiple rows
         * @return ResultSet
         */
        public ResultSet getResultSet() throws SQLException {
            SimpleResultSet rs = new SimpleResultSet(this);
            // Feed with fields
            TableUtilities.copyFields(connection, rs, tableLocation);
            rs.addColumn(EXPLODE_FIELD, Types.INTEGER,10,0);
            return rs;
        }
    }
    
    /**
     * Explode fields only on request
     * The input data must be a SELECT  expression that contains a geometry column
     */
    public static class ExplodeResultSetQuery extends ExplodeResultSet {

        public ExplodeResultSetQuery(Connection connection, String tableName, String spatialFieldName) throws SQLException {
            super(connection, tableName, spatialFieldName);
        }

        @Override
        public void reset() throws SQLException {
            if (tableQuery != null && !tableQuery.isClosed()) {
                close();
            }
            Statement st = connection.createStatement();
            tableQuery = st.executeQuery(tableName);
            firstRow = false;
        }
        
        @Override
        public ResultSet getResultSet() throws SQLException {            
            SimpleResultSet rs = new SimpleResultSet(this);
            // Feed with fields
            copyfields(rs, tableName);
            rs.addColumn(EXPLODE_FIELD, Types.INTEGER,10,0);
            return rs;
        }

        /**
         * Perform a fast copy of columns using a limit clause.
         * @param rs {@link SimpleResultSet}
         * @param selectQuery select query
         */
        private void copyfields(SimpleResultSet rs, String selectQuery) throws SQLException { 
            Statement st = null;
            ResultSet rsQuery = null;            
            st = connection.createStatement();
            try {
                rsQuery = st.executeQuery(limitQuery(selectQuery.toUpperCase()));
                ResultSetMetaData metadata = rsQuery.getMetaData();
                columnCount = metadata.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {

                    String type = metadata.getColumnTypeName(i);
                    String columnName =metadata.getColumnName(i);
                    String label = metadata.getColumnLabel(i);
                    if(label!=null){
                        columnName = label;
                    }
                    //TODO : workarround due to the geometry type signature returned by H2  eg. GEOMETRY(POLYGON)
                    if (type.toLowerCase().startsWith("geometry")&& spatialFieldIndex==-1) {
                        spatialFieldIndex = i;
                        rs.addColumn(columnName, metadata.getColumnType(i),
                                "GEOMETRY", metadata.getPrecision(i), metadata.getScale(i));
                    }
                    else{
                        rs.addColumn(columnName, metadata.getColumnType(i),
                                type, metadata.getPrecision(i), metadata.getScale(i));
                    }
                }

            } catch (SQLException ex) {
                throw new SQLException(ex);
            }
            if (spatialFieldIndex == -1) {
                throw new SQLException("The select query " + selectQuery + " does not contain a geometry field");
            }
        }

        /**
         * Method to perform the select query with a limit clause
         * @param selectQuery input select query
         * @return select + limit query
         */
        private String limitQuery(String selectQuery) {
            //Remove the parentheses
            selectQuery =  selectQuery.substring(1, selectQuery.lastIndexOf(")"));
            int findLIMIT = selectQuery.lastIndexOf("LIMIT ");
            int comma = selectQuery.lastIndexOf(";");
            if (findLIMIT == -1) {
                if (comma == -1) {
                    selectQuery += " LIMIT 0;";
                } else {
                    selectQuery = selectQuery.substring(0, comma) + " LIMIT 0;";
                }
            } else {
                selectQuery = selectQuery.substring(0, findLIMIT) + " LIMIT 0;";
            }
            return selectQuery;
        }
    }
}
