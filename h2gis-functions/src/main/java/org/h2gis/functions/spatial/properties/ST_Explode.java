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

package org.h2gis.functions.spatial.properties;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

/**
 * This table function explode Geometry Collection into multiple geometries
 * @author Nicolas Fortin
 */
public class ST_Explode extends AbstractFunction implements ScalarFunction {
    /** The default field name for explode count, value is [1-n] */
    public static final String EXPLODE_FIELD = "EXPLOD_ID";

    public ST_Explode() {
        addProperty(PROP_REMARKS, "Explode Geometry Collection into multiple geometries.\n"
                + "Note : This function supports select query as the first arfument.");
        addProperty(PROP_NOBUFFER, true);
    }

    @Override
    public String getJavaStaticMethod() {
        return "explode";
    }

    /**
     * Explode Geometry Collection into multiple geometries
     * @param connection
     * @param tableName the name of the input table or select query
     * @return A result set with the same content of specified table but with atomic geometries and duplicate values.
     * @throws java.sql.SQLException
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
     * @param connection
     * @param tableName the name of the input table
     * @param fieldName the name of geometry field. If null the first geometry column is used.
     * @return
     * @throws java.sql.SQLException
     */
    public static ResultSet explode(Connection connection, String tableName, String fieldName) throws SQLException {
        ExplodeResultSet rowSource = new ExplodeResultSet(connection,
                TableLocation.parse(tableName, JDBCUtilities.isH2DataBase(connection.getMetaData())).toString(),fieldName);
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
        
        public ExplodeResultSet(Connection connection, String tableName, String spatialFieldName) {
            this.tableName = tableName;
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
         * @throws SQLException 
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
            Statement st = connection.createStatement();
            tableQuery = st.executeQuery("SELECT * FROM "+tableName);
            firstRow = false;
            ResultSetMetaData meta = tableQuery.getMetaData();
            columnCount = meta.getColumnCount();
            if(spatialFieldName==null) {
                // Find first geometry column
                List<String> geomFields = SFSUtilities.getGeometryFields(connection,TableLocation.parse(tableName));
                if(!geomFields.isEmpty()) {
                    spatialFieldName = geomFields.get(0);
                } else {
                    throw new SQLException("The table "+tableName+" does not contain a geometry field");
                }
            }
            for(int i=1;i<=columnCount;i++) {
                if(meta.getColumnName(i).equalsIgnoreCase(spatialFieldName)) {
                    spatialFieldIndex = i;
                    break;
                }
            }
            if(spatialFieldIndex==-1) {
                throw new SQLException("Geometry field "+spatialFieldName+" of table "+tableName+" not found");
            }
        }

        /**
         * Return the exploded geometries as multiple rows
         * @return
         * @throws SQLException 
         */
        public ResultSet getResultSet() throws SQLException {
            SimpleResultSet rs = new SimpleResultSet(this);
            // Feed with fields
            TableUtilities.copyFields(connection, rs, TableLocation.parse(tableName, JDBCUtilities.isH2DataBase(connection.getMetaData())));
            rs.addColumn(EXPLODE_FIELD, Types.INTEGER,10,0);
            return rs;
        }
    }
    
    /**
     * Explode fields only on request
     * The input data must be a SELECT  expression that contains a geometry column
     */
    public static class ExplodeResultSetQuery extends ExplodeResultSet {

        public ExplodeResultSetQuery(Connection connection, String tableName, String spatialFieldName) {
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
         * @param rs
         * @param selectQuery
         * @throws SQLException 
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
                    if (metadata.getColumnTypeName(i).equalsIgnoreCase("geometry")&& spatialFieldIndex==-1) {
                        spatialFieldIndex = i;
                    }
                    rs.addColumn(metadata.getColumnName(i), metadata.getColumnType(i),
                            metadata.getColumnTypeName(i), metadata.getPrecision(i), metadata.getScale(i));
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
         * @param selectQuery
         * @return 
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
