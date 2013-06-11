/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.h2spatialext.function.spatial.table;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.orbisgis.sputilities.SFSUtilities;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This table function explode Geometry Collection into multiple geometries
 * @author Nicolas Fortin
 */
public class ST_Explode implements ScalarFunction {
    /** The default field name for explode count, value is [1-n] */
    public static final String EXPLODE_FIELD = "EXPLOD_ID";

    @Override
    public String getJavaStaticMethod() {
        return "explode";
    }

    @Override
    public Object getProperty(String propertyName) {
        return null;
    }

    /**
     * Explode Geometry Collection into multiple geometries
     * @param connection
     * @param tableName
     * @return A result set with the same content of specified table but with atomic geometries and duplicate values.
     */
    public static ResultSet explode(Connection connection, String tableName) throws SQLException {
        return explode(connection, tableName,null);
    }

    /**
     *
     * @param connection
     * @param tableName
     * @param fieldName
     * @return
     */
    public static ResultSet explode(Connection connection, String tableName, String fieldName) throws SQLException {
        ExplodeResultSet rowSource = new ExplodeResultSet(connection,tableName,fieldName);
        return rowSource.getResultSet();
    }

    /**
     * Explode fields only on request
     */
    private static class ExplodeResultSet implements SimpleRowSource {
        // If true, table query is closed the read again
        private boolean firstRow = true;
        private ResultSet tableQuery;
        private String tableName;
        private String spatialFieldName;
        private Integer spatialFieldIndex;
        private int columnCount;
        private Queue<Geometry> sourceRowGeometries = new LinkedList<Geometry>();
        private int explodeId = 1;
        private Connection connection;
        private ExplodeResultSet(Connection connection, String tableName, String spatialFieldName) {
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
                    } else if(geometry instanceof MultiPoint) {
                        sourceRowGeometries.add((factory.createPoint((Coordinate)null)));
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
                List<String> geomFields = SFSUtilities.getGeometryFields(connection,SFSUtilities.splitCatalogSchemaTableName(tableName));
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
            if(spatialFieldIndex == null) {
                throw new SQLException("Geometry field "+spatialFieldName+" of table "+tableName+" not found");
            }
        }

        public ResultSet getResultSet() throws SQLException {
            SimpleResultSet rs = new SimpleResultSet(this);
            // Feed with fields
            reset();
            ResultSetMetaData meta = tableQuery.getMetaData();
            for(int i=1;i<=columnCount;i++) {
                rs.addColumn(meta.getColumnName(i),meta.getColumnType(i),meta.getPrecision(i),meta.getScale(i));
            }
            rs.addColumn(EXPLODE_FIELD, Types.INTEGER,10,0);
            return rs;
        }
    }
}
