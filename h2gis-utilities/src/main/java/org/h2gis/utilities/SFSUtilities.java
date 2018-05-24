/*
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

package org.h2gis.utilities;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.DataSourceWrapper;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Generic utilities function to retrieve spatial metadata trough SFS specification.
 * Compatible with H2 and PostGIS.
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class SFSUtilities {

    private static final Map<Integer, String> TYPE_MAP = new HashMap<>();
    private static final Map<String, Integer> GEOM_TYPE_TO_SFS_CODE;
    static {
        GEOM_TYPE_TO_SFS_CODE = new HashMap<>();
        GEOM_TYPE_TO_SFS_CODE.put("point", GeometryTypeCodes.POINT);
        GEOM_TYPE_TO_SFS_CODE.put("linestring", GeometryTypeCodes.LINESTRING);
        GEOM_TYPE_TO_SFS_CODE.put("polygon", GeometryTypeCodes.POLYGON);
        GEOM_TYPE_TO_SFS_CODE.put("multipoint", GeometryTypeCodes.MULTIPOINT);
        GEOM_TYPE_TO_SFS_CODE.put("multilinestring", GeometryTypeCodes.MULTILINESTRING);
        GEOM_TYPE_TO_SFS_CODE.put("multipolygon", GeometryTypeCodes.MULTIPOLYGON);
        GEOM_TYPE_TO_SFS_CODE.put("geometry", GeometryTypeCodes.GEOMETRY);
        GEOM_TYPE_TO_SFS_CODE.put("geometrycollection", GeometryTypeCodes.GEOMCOLLECTION);
        // Cache GeometryTypeCodes into a static HashMap
        for(Field field : GeometryTypeCodes.class.getDeclaredFields()) {
            try {
                TYPE_MAP.put(field.getInt(null),field.getName());
            } catch (IllegalAccessException ignored) {}
        }
    }

    public static String getGeometryTypeNameFromCode(int geometryTypeCode) {
        return TYPE_MAP.get(geometryTypeCode);
    }

    /**
     * Return the sfs geometry type identifier of the provided Geometry
     *
     * @param geometry Geometry instance
     *
     * @return The sfs geometry type identifier
     */
    public static int getGeometryTypeFromGeometry(Geometry geometry) {
        Integer sfsGeomCode = GEOM_TYPE_TO_SFS_CODE.get(geometry.getGeometryType().toLowerCase());
        if(sfsGeomCode == null) {
            return GeometryTypeCodes.GEOMETRY;
        } else {
            return sfsGeomCode;
        }
    }

    /**
     * Return the sfs geometry type identifier of the provided field of the provided table.
     *
     * @param connection Active connection
     * @param location Catalog, schema and table name
     * @param fieldName Geometry field name or empty (take the first one)
     *
     * @return The sfs geometry type identifier
     *
     * @see GeometryTypeCodes
     *
     * @throws SQLException
     */
    public static int getGeometryType(Connection connection,TableLocation location, String fieldName)
            throws SQLException {
        if(fieldName==null || fieldName.isEmpty()) {
            List<String> geometryFields = getGeometryFields(connection, location);
            if(geometryFields.isEmpty()) {
                throw new SQLException("The table "+location+" does not contain a Geometry field, " +
                        "then geometry type cannot be computed");
            }
            fieldName = geometryFields.get(0);
        }
        ResultSet geomResultSet = getGeometryColumnsView(connection,location.getCatalog(),location.getSchema(),
                location.getTable());
        boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        while(geomResultSet.next()) {
            if(fieldName.isEmpty() || geomResultSet.getString("F_GEOMETRY_COLUMN").equalsIgnoreCase(fieldName)) {
                if(isH2) {
                    return geomResultSet.getInt("GEOMETRY_TYPE");
                } else {
                    return GEOM_TYPE_TO_SFS_CODE.get(geomResultSet.getString("type").toLowerCase());
                }
            }
        }
        throw new SQLException("Field not found "+fieldName);
    }

    /**
     * Returns a map containing the field names as key and the SFS geometry type as value from the given table.
     *
     * @param connection Active connection
     * @param location Catalog, schema and table name
     *
     * @return A map containing the geometric fields names as key and the SFS geometry type as value.
     *
     * @see GeometryTypeCodes
     *
     * @throws SQLException
     */
    public static Map<String, Integer> getGeometryTypes(Connection connection, TableLocation location)
            throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        ResultSet geomResultSet = getGeometryColumnsView(connection,location.getCatalog(),location.getSchema(),
                location.getTable());
        boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        while(geomResultSet.next()) {
            String fieldName = geomResultSet.getString("F_GEOMETRY_COLUMN");
            int type;
            if(isH2) {
                type = geomResultSet.getInt("GEOMETRY_TYPE");
            } else {
                type = GEOM_TYPE_TO_SFS_CODE.get(geomResultSet.getString("type").toLowerCase());
            }
            map.put(fieldName, type);
        }
        return map;
    }


    /**
     * In order to be able to use {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get {@link SpatialResultSet} and
     * {@link SpatialResultSetMetaData} this method wrap the provided dataSource.
     *
     * @param dataSource H2 or PostGIS DataSource
     *
     * @return Wrapped DataSource, with spatial methods
     */
    public static DataSource wrapSpatialDataSource(DataSource dataSource) {
        try {
            if(dataSource.isWrapperFor(DataSourceWrapper.class)) {
                return dataSource;
            } else {
                return new DataSourceWrapper(dataSource);
            }
        } catch (SQLException ex) {
            return new DataSourceWrapper(dataSource);
        }
    }

    /**
     * Use this only if DataSource is not available.
     * In order to be able to use {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get {@link SpatialResultSet} and
     * {@link SpatialResultSetMetaData} this method wrap the provided connection.
     *
     * @param connection H2 or PostGIS Connection
     *
     * @return Wrapped DataSource, with spatial methods
     */
    public static Connection wrapConnection(Connection connection) {
        try {
            if(connection.isWrapperFor(ConnectionWrapper.class)) {
                return connection;
            } else {
                return new ConnectionWrapper(connection);
            }
        } catch (SQLException ex) {
            return new ConnectionWrapper(connection);
        }
    }

    /**
     * Merge the bounding box of all geometries inside the provided table.
     *
     * @param connection Active connection (not closed by this function)
     * @param location Location of the table
     * @param geometryField Geometry field or empty string (take the first geometry field)
     *
     * @return Envelope of the table
     *
     * @throws SQLException If the table not exists, empty or does not contain a geometry field.
     */
    public static Envelope getTableEnvelope(Connection connection, TableLocation location, String geometryField)
            throws SQLException {
        if(geometryField==null || geometryField.isEmpty()) {
            List<String> geometryFields = getGeometryFields(connection, location);
            if(geometryFields.isEmpty()) {
                throw new SQLException("The table "+location+" does not contain a Geometry field, then the extent " +
                        "cannot be computed");
            }
            geometryField = geometryFields.get(0);
        }
        ResultSet rs = connection.createStatement().executeQuery("SELECT ST_Extent("+
                TableLocation.quoteIdentifier(geometryField)+") ext FROM "+location);
        if(rs.next()) {
            // Todo under postgis it is a BOX type
            return ((Geometry)rs.getObject(1)).getEnvelopeInternal();
        }
        throw new SQLException("Unable to get the table extent it may be empty");
    }

    /**
     * Find geometry fields name of a table.
     *
     * @param connection Active connection
     * @param location Table location
     *
     * @return A list of Geometry fields name
     *
     * @throws SQLException
     */
    public static List<String> getGeometryFields(Connection connection,TableLocation location) throws SQLException {
        return getGeometryFields(connection, location.getCatalog(), location.getSchema(), location.getTable());
    }

    /**
     * For table containing catalog, schema and table name, this function create a prepared statement with a filter
     * on this combination.
     *
     * @param connection Active connection
     * @param catalog Table catalog, may be empty
     * @param schema Table schema, may be empty
     * @param table Table name
     * @param informationSchemaTable Information table location
     * @param endQuery Additional where statement
     * @param catalog_field Catalog field name
     * @param schema_field Schema field name
     * @param table_field Table field name
     *
     * @return Prepared statement
     *
     * @throws SQLException
     */
    public static PreparedStatement prepareInformationSchemaStatement(Connection connection,String catalog,
                                                                      String schema, String table,
                                                                      String informationSchemaTable,
                                                                      String endQuery, String catalog_field,
                                                                      String schema_field, String table_field)
            throws SQLException {
        Integer catalogIndex = null;
        Integer schemaIndex = null;
        Integer tableIndex = 1;
        StringBuilder sb = new StringBuilder("SELECT * from "+informationSchemaTable+" where ");
        if(!catalog.isEmpty()) {
            sb.append("UPPER(");
            sb.append(catalog_field);
            sb.append(") = ? AND ");
            catalogIndex = 1;
            tableIndex++;
        }
        if(!schema.isEmpty()) {
            sb.append("UPPER(");
            sb.append(schema_field);
            sb.append(") = ? AND ");
            schemaIndex = tableIndex;
            tableIndex++;
        }
        sb.append("UPPER(");
        sb.append(table_field);
        sb.append(") = ? ");
        sb.append(endQuery);
        PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
        if(catalogIndex!=null) {
            preparedStatement.setString(catalogIndex, catalog.toUpperCase());
        }
        if(schemaIndex!=null) {
            preparedStatement.setString(schemaIndex, schema.toUpperCase());
        }
        preparedStatement.setString(tableIndex, table.toUpperCase());
        return preparedStatement;
    }

    /**
     * For table containing catalog, schema and table name, this function create a prepared statement with a filter on
     * this combination. Use "f_table_catalog","f_table_schema","f_table_name" as field names.
     *
     * @param connection Active connection
     * @param catalog Table catalog, may be empty
     * @param schema Table schema, may be empty
     * @param table Table name
     * @param informationSchemaTable Information table location
     * @param endQuery Additional where statement
     *
     * @return Prepared statement
     *
     * @throws SQLException
     */
    public static PreparedStatement prepareInformationSchemaStatement(Connection connection,String catalog,
                                                                      String schema, String table,
                                                                      String informationSchemaTable, String endQuery)
            throws SQLException {
        return prepareInformationSchemaStatement(connection,catalog, schema, table, informationSchemaTable, endQuery,
                "f_table_catalog","f_table_schema","f_table_name");
    }

    private static ResultSet getGeometryColumnsView(Connection connection,String catalog, String schema, String table)
            throws SQLException {
        PreparedStatement geomStatement = prepareInformationSchemaStatement(connection,catalog, schema, table,
                "geometry_columns", "");
        return geomStatement.executeQuery();
    }
    /**
     * Find geometry fields name of a table.
     *
     * @param connection Active connection
     * @param catalog Catalog that contain schema (empty for default catalog)
     * @param schema Schema that contain table (empty for default schema)
     * @param table Table name (case insensitive)
     *
     * @return A list of Geometry fields name
     *
     * @throws SQLException
     */
    public static List<String> getGeometryFields(Connection connection,String catalog, String schema, String table)
            throws SQLException {
        List<String> fieldsName = new LinkedList<>();
        ResultSet geomResultSet = getGeometryColumnsView(connection,catalog,schema,table);
        while (geomResultSet.next()) {
            fieldsName.add(geomResultSet.getString("f_geometry_column"));
        }
        geomResultSet.close();
        return fieldsName;
    }
    
    /**
     * Find geometry fields name of a resultSet.
     *
     * @param resultSet ResultSet to analyse
     *
     * @return A list of Geometry fields name
     *
     * @throws SQLException
     */
    public static List<String> getGeometryFields(ResultSet resultSet) throws SQLException {
        List<String> fieldsName = new LinkedList<>();
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (meta.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                fieldsName.add(meta.getColumnName(i));
            }
        }
        return fieldsName;
    }   
    

    /**
     * Find the first geometry field name of a resultSet. Return -1 if there is
     * no geometry column
     *
     * @param resultSet ResultSet to analyse
     *
     * @return The index of first Geometry field
     *
     * @throws SQLException
     */
    public static int getFirstGeometryFieldIndex(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (meta.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Find the first geometry field name of a resultSet. 
     * Return -1 if there is no geometry column
     *
     * @param resultSet ResultSet to analyse
     *
     * @return The name of first geometry field
     *
     * @throws SQLException
     */
    public static String getFirstGeometryFieldName(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (meta.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                return meta.getColumnName(i);
            }
        }
        throw new SQLException("The query doesn't contain any geometry field");
    }

    /**
     * Check if the ResultSet contains a geometry field
     *
     * @param resultSet ResultSet to analyse
     *
     * @return True if the ResultSet contains one geometry field
     *
     * @throws SQLException
     */
    public static boolean hasGeometryField(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (meta.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compute the full extend of a ResultSet using the first geometry field. If
     * the ResultSet does not contain any geometry field throw an exception
     *
     * @param resultSet ResultSet to analyse
     *
     * @return The full envelope of the ResultSet
     *
     * @throws SQLException
     */
    public static Envelope getResultSetEnvelope(ResultSet resultSet) throws SQLException {
        List<String> geometryFields = getGeometryFields(resultSet);
        if (geometryFields.isEmpty()) {
            throw new SQLException("This ResultSet doesn't contain any geometry field.");
        } else {
            return getResultSetEnvelope(resultSet, geometryFields.get(0));
        }
    }

    /**
     * Compute the full extend of a ResultSet using a specified geometry field.
     * If the ResultSet does not contain this geometry field throw an exception
     *
     * @param resultSet ResultSet to analyse
     * @param fieldName Field to analyse
     *
     * @return The full extend of the field in the ResultSet
     *
     * @throws SQLException
     */
    public static Envelope getResultSetEnvelope(ResultSet resultSet, String fieldName) throws SQLException {
        Envelope aggregatedEnvelope = null;
        while (resultSet.next()) {
            Geometry geom = (Geometry) resultSet.getObject(fieldName);
            if (aggregatedEnvelope != null) {
                aggregatedEnvelope.expandToInclude(geom.getEnvelopeInternal());
            } else {
                aggregatedEnvelope = geom.getEnvelopeInternal();
            }
        }
        return aggregatedEnvelope;
    }
    
    /**
     * Return the SRID of the first geometry column of the input table
     *
     * @param connection Active connection
     * @param table Table name
     *
     * @return The SRID of the first geometry column
     *
     * @throws SQLException 
     */
    public static int getSRID(Connection connection, TableLocation table) throws SQLException {
        ResultSet geomResultSet = getGeometryColumnsView(connection, table.getCatalog(), table.getSchema(),
                table.getTable());
        int srid = 0;
        while (geomResultSet.next()) {
            srid = geomResultSet.getInt("srid");
            break;
        }
        geomResultSet.close();
        return srid;
    }
    
    /**
     * Return the srid of a table for a given field name. If the specified field isn't geometric, return 0.
     *
     * @param connection Active connection
     * @param table Table name
     * @param fieldName Field to analyse
     *
     * @return The SRID of the field
     *
     * @throws SQLException
     */
    public static int getSRID(Connection connection, TableLocation table, String fieldName) throws SQLException {
        ResultSet geomResultSet = getGeometryColumnsView(connection, table.getCatalog(), table.getSchema(),
                table.getTable());
        int srid = 0;
        while (geomResultSet.next()) {
            if (geomResultSet.getString("f_geometry_column").equals(fieldName)) {
                srid = geomResultSet.getInt("srid");
                break;
            }
        }
        geomResultSet.close();
        return srid;
    }
    
    /**
     * Return an array of two string that correspond to the authority name and its SRID code.
     * If the SRID does not exist return the array {null, null}
     *
     * @param connection Active connection
     * @param table Table name
     * @param fieldName Field to analyse
     *
     * @return Array of two string that correspond to the authority name  and its SRID code
     *
     * @throws SQLException 
     */
    public static String[] getAuthorityAndSRID(Connection connection, TableLocation table, String fieldName)
            throws SQLException{
        ResultSet geomResultSet = getGeometryColumnsView(connection, table.getCatalog(), table.getSchema(),
                table.getTable());
        int srid = 0;
        while (geomResultSet.next()) {
            if (geomResultSet.getString("f_geometry_column").equals(fieldName)) {
                srid = geomResultSet.getInt("srid");
                break;
            }
        }
        geomResultSet.close();
        String authority = null;
        String sridCode = null;
        if (srid != 0) {
            PreparedStatement ps = connection.prepareStatement("SELECT AUTH_NAME FROM PUBLIC.SPATIAL_REF_SYS " +
                    " WHERE SRID = ?");
            ps.setInt(1, srid);
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                if (rs.next()) {
                    authority = rs.getString(1);
                    sridCode=String.valueOf(srid);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                ps.close();
            }
        }
        return new String[]{authority, sridCode};
    }
    
    
    /**
     * Alter a table to add a SRID constraint.
     * The srid must be greater than zero.
     * 
     * @param connection Active connection
     * @param tableLocation TableLocation of the table to update
     * @param srid SRID to set
     *
     * @throws SQLException 
     */
    public static void addTableSRIDConstraint(Connection connection, TableLocation tableLocation, int srid)
            throws SQLException {
        //Alter table to set the SRID constraint
        if (srid > 0) {
            connection.createStatement().execute(String.format("ALTER TABLE %s ADD CHECK ST_SRID(the_geom)=%d",
                    tableLocation.toString(), srid));
        }
    }
}
