/*
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

package org.orbisgis.sputilities;

import com.vividsolutions.jts.geom.Envelope;
import org.orbisgis.sputilities.wrapper.ConnectionWrapper;
import org.orbisgis.sputilities.wrapper.DataSourceWrapper;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic utilities function to retrieve spatial metadata trough SFS specification.
 * Compatible with H2 and PostGIS.
 * @author Nicolas Fortin
 */
public class SFSUtilities {


    /**
     * @param connection Active connection
     * @param location Catalog, schema and table name
     * @param fieldName Geometry field name or empty (take the first one)
     * @return The geometry type identifier
     * @see GeometryTypeCodes
     * @throws SQLException
     */
    public static int getGeometryType(Connection connection,TableLocation location, String fieldName) throws SQLException {
        if(fieldName==null || fieldName.isEmpty()) {
            List<String> geometryFields = getGeometryFields(connection, location);
            if(geometryFields.isEmpty()) {
                throw new SQLException("The table "+location+" does not contain a Geometry field, then geometry type cannot be computed");
            }
            fieldName = geometryFields.get(0);
        }
        ResultSet geomResultSet = getGeometryColumnsView(connection,location.getCatalog(),location.getSchema(),location.getTable());
        while(geomResultSet.next()) {
            if(fieldName.isEmpty() || geomResultSet.getString("F_GEOMETRY_COLUMN").equalsIgnoreCase(fieldName)) {
                return geomResultSet.getInt("GEOMETRY_TYPE");
            }
        }
        throw new SQLException("Field not found "+fieldName);
    }


    /**
     * In order to be able to use {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get {@link SpatialResultSet} and
     * {@link SpatialResultSetMetaData} this method wrap the provided dataSource.
     * @param dataSource H2 or PostGIS DataSource
     * @return Wrapped DataSource, with spatial methods
     */
    public static DataSource wrapSpatialDataSource(DataSource dataSource) {
        return new DataSourceWrapper(dataSource);
    }

    /**
     * Use this only if DataSource is not available.
     * In order to be able to use {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get {@link SpatialResultSet} and
     * {@link SpatialResultSetMetaData} this method wrap the provided connection.
     * @param connection H2 or PostGIS Connection
     * @return Wrapped DataSource, with spatial methods
     */
    public static Connection wrapConnection(Connection connection) {
        return new ConnectionWrapper(connection,null);
    }

    /**
     * Convert catalog.schema.table, schema.table or table into TableLocation instance.
     * Not specified schema or catalog are converted into an empty string.
     * @param concatenatedTableLocation Table location
     * @return Java beans for table location
     */
    public static TableLocation splitCatalogSchemaTableName(String concatenatedTableLocation) {
        return TableLocation.parse(concatenatedTableLocation);
    }

    /**
     * Merge the bounding box of all geometries inside the provided table.
     * @param connection Active connection (not closed by this function)
     * @param location Location of the table
     * @param geometryField Geometry field or empty string (take the first geometry field)
     * @return Envelope of the table
     * @throws SQLException If the table not exists, empty or does not contain a geometry field.
     */
    public static Envelope getTableEnvelope(Connection connection, TableLocation location, String geometryField) throws SQLException {
        if(geometryField==null || geometryField.isEmpty()) {
            List<String> geometryFields = getGeometryFields(connection, location);
            if(geometryFields.isEmpty()) {
                throw new SQLException("The table "+location+" does not contain a Geometry field, then the extent cannot be computed");
            }
            geometryField = geometryFields.get(0);
        }
        ResultSet rs = connection.createStatement().executeQuery("SELECT ST_Extent("+geometryField+") ext FROM "+location);
        if(rs.next()) {
            // Todo under postgis it is a BOX type
            return (Envelope)rs.getObject(1);
        }
        throw new SQLException("Unable to get the table extent it may be empty");
    }

    /**
     * Find geometry fields name of a table.
     * @param connection Active connection
     * @param location Table location
     * @return A list of Geometry fields name
     * @throws SQLException
     */
    public static List<String> getGeometryFields(Connection connection,TableLocation location) throws SQLException {
        return getGeometryFields(connection, location.getCatalog(), location.getSchema(), location.getTable());
    }

    private static ResultSet getGeometryColumnsView(Connection connection,String catalog, String schema, String table) throws SQLException {
        Integer catalogIndex = null;
        Integer schemaIndex = null;
        Integer tableIndex = 1;
        StringBuilder sb = new StringBuilder("SELECT * from geometry_columns where ");
        if(!catalog.isEmpty()) {
            sb.append("UPPER(f_catalog_name) = ? AND ");
            catalogIndex = 1;
            tableIndex++;
        }
        if(!schema.isEmpty()) {
            sb.append("UPPER(f_schema_name) = ? AND ");
            schemaIndex = tableIndex;
            tableIndex++;
        }
        sb.append("UPPER(f_table_name) = ? ");
        PreparedStatement geomStatement = connection.prepareStatement(sb.toString());
        if(catalogIndex!=null) {
            geomStatement.setString(catalogIndex,catalog.toUpperCase());
        }
        if(schemaIndex!=null) {
            geomStatement.setString(schemaIndex,schema.toUpperCase());
        }
        geomStatement.setString(tableIndex,table.toUpperCase());
        return geomStatement.executeQuery();
    }
    /**
     * Find geometry fields name of a table.
     * @param connection Active connection
     * @param catalog Catalog that contain schema (empty for default catalog)
     * @param schema Schema that contain table (empty for default schema)
     * @param table Table name (case insensitive)
     * @return A list of Geometry fields name
     * @throws SQLException
     */
    public static List<String> getGeometryFields(Connection connection,String catalog, String schema, String table) throws SQLException {
        List<String> fieldsName = new LinkedList<String>();
        ResultSet geomResultSet = getGeometryColumnsView(connection,catalog,schema,table);
        while (geomResultSet.next()) {
            fieldsName.add(geomResultSet.getString("f_geometry_column"));
        }
        geomResultSet.close();
        return fieldsName;
    }
}
