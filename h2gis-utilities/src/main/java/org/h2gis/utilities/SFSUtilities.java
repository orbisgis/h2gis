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

import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.DataSourceWrapper;
import org.locationtech.jts.geom.Geometry;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.cts.util.UTMUtils;

/**
 * Generic utilities function to retrieve spatial metadata trough SFS
 * specification. Compatible with H2 and PostGIS.
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class SFSUtilities {

    private static final Map<Integer, String> CODE_TYPE_MAP = new HashMap<>();
    private static final Map<String, Integer> TYPE_CODE_MAP = new HashMap<>();
    static {
        // Cache GeometryTypeCodes into a static HashMap
        for (Field field : GeometryTypeCodes.class.getDeclaredFields()) {
            try {
                CODE_TYPE_MAP.put(field.getInt(null), field.getName());
                TYPE_CODE_MAP.put(field.getName(),field.getInt(null));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static String getGeometryTypeNameFromCode(int geometryTypeCode) {
        return CODE_TYPE_MAP.get(geometryTypeCode);
    }

    /**
     * Return the sfs geometry type identifier of the provided Geometry
     *
     * @param geometry Geometry instance
     *
     * @return The sfs geometry type identifier
     */
    public static int getGeometryTypeFromGeometry(Geometry geometry) {
        return GeometryMetaData.getMetaData(geometry).getGeometryTypeCode();
    }

    /**
     * Return the sfs geometry type identifier of the provided field of the
     * provided table.
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
    public static int getGeometryType(Connection connection, TableLocation location, String fieldName)
            throws SQLException {
        if (fieldName == null || fieldName.isEmpty()) {
            List<String> geometryFields = GeometryTableUtils.getGeometryFields(connection, location);
            if (geometryFields.isEmpty()) {
                throw new SQLException("The table " + location + " does not contain a Geometry field, "
                        + "then geometry type cannot be computed");
            }
            fieldName = geometryFields.get(0);
        }
        ResultSet geomResultSet = GeometryTableUtils.getGeometryColumnsView(connection, location.getCatalog(), location.getSchema(),
                location.getTable());
        boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        while (geomResultSet.next()) {
            if (fieldName.isEmpty() || geomResultSet.getString("F_GEOMETRY_COLUMN").equalsIgnoreCase(fieldName)) {
                if (isH2) {
                    return geomResultSet.getInt("GEOMETRY_TYPE");
                } else {
                    return TYPE_CODE_MAP.get(geomResultSet.getString("type").toLowerCase());
                }
            }
        }
        throw new SQLException("Field not found " + fieldName);
    }

    /**
     * Returns a map containing the field names as key and the SFS geometry type
     * as value from the given table.
     *
     * @param connection Active connection
     * @param location Catalog, schema and table name
     *
     * @return A map containing the geometric fields names as key and the SFS
     * geometry type as value.
     *
     * @see GeometryTypeCodes
     *
     * @throws SQLException
     */
    public static Map<String, Integer> getGeometryTypes(Connection connection, TableLocation location)
            throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        ResultSet geomResultSet = GeometryTableUtils.getGeometryColumnsView(connection, location.getCatalog(), location.getSchema(),
                location.getTable());
        boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        while (geomResultSet.next()) {
            String fieldName = geomResultSet.getString("F_GEOMETRY_COLUMN");
            int type;
            if (isH2) {
                type = geomResultSet.getInt("GEOMETRY_TYPE");
            } else {
                type = TYPE_CODE_MAP.get(geomResultSet.getString("type").toLowerCase());
            }
            map.put(fieldName, type);
        }
        return map;
    }

    /**
     * In order to be able to use {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get
     * {@link SpatialResultSet} and {@link SpatialResultSetMetaData} this method
     * wrap the provided dataSource.
     *
     * @param dataSource H2 or PostGIS DataSource
     *
     * @return Wrapped DataSource, with spatial methods
     */
    public static DataSource wrapSpatialDataSource(DataSource dataSource) {
        try {
            if (dataSource.isWrapperFor(DataSourceWrapper.class)) {
                return dataSource;
            } else {
                return new DataSourceWrapper(dataSource);
            }
        } catch (SQLException ex) {
            return new DataSourceWrapper(dataSource);
        }
    }

    /**
     * Use this only if DataSource is not available. In order to be able to use
     * {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get
     * {@link SpatialResultSet} and {@link SpatialResultSetMetaData} this method
     * wrap the provided connection.
     *
     * @param connection H2 or PostGIS Connection
     *
     * @return Wrapped DataSource, with spatial methods
     */
    public static Connection wrapConnection(Connection connection) {
        try {
            if (connection.isWrapperFor(ConnectionWrapper.class)) {
                return connection;
            } else {
                return new ConnectionWrapper(connection);
            }
        } catch (SQLException ex) {
            return new ConnectionWrapper(connection);
        }
    }
    

    /**
     * For table containing catalog, schema and table name, this function create
     * a prepared statement with a filter on this combination.
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
    public static PreparedStatement prepareInformationSchemaStatement(Connection connection, String catalog,
            String schema, String table,
            String informationSchemaTable,
            String endQuery, String catalog_field,
            String schema_field, String table_field)
            throws SQLException {
        Integer catalogIndex = null;
        Integer schemaIndex = null;
        Integer tableIndex = 1;
        StringBuilder sb = new StringBuilder("SELECT * from " + informationSchemaTable + " where ");
        if (!catalog.isEmpty()) {
            sb.append("UPPER(");
            sb.append(catalog_field);
            sb.append(") = ? AND ");
            catalogIndex = 1;
            tableIndex++;
        }
        if (!schema.isEmpty()) {
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
        if (catalogIndex != null) {
            preparedStatement.setString(catalogIndex, catalog.toUpperCase());
        }
        if (schemaIndex != null) {
            preparedStatement.setString(schemaIndex, schema.toUpperCase());
        }
        preparedStatement.setString(tableIndex, table.toUpperCase());
        return preparedStatement;
    }

    /**
     * For table containing catalog, schema and table name, this function create
     * a prepared statement with a filter on this combination. Use
     * "f_table_catalog","f_table_schema","f_table_name" as field names.
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
    public static PreparedStatement prepareInformationSchemaStatement(Connection connection, String catalog,
            String schema, String table,
            String informationSchemaTable, String endQuery)
            throws SQLException {
        return prepareInformationSchemaStatement(connection, catalog, schema, table, informationSchemaTable, endQuery,
                "f_table_catalog", "f_table_schema", "f_table_name");
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
     * Return an array of two string that correspond to the authority name and
     * its SRID code. If the SRID does not exist return the array {null, null}
     *
     * @param connection Active connection
     * @param table Table name
     * @param fieldName Field to analyse
     *
     * @return Array of two string that correspond to the authority name and its
     * SRID code
     *
     * @throws SQLException
     */
    public static String[] getAuthorityAndSRID(Connection connection, TableLocation table, String fieldName)
            throws SQLException {
        int srid;
        try (ResultSet geomResultSet = GeometryTableUtils.getGeometryColumnsView(connection, table.getCatalog(), table.getSchema(),
                table.getTable(),fieldName)) {
            srid = 0;
            while (geomResultSet.next()) {
                if (geomResultSet.getString("f_geometry_column").equals(fieldName)) {
                    srid = geomResultSet.getInt("srid");
                    break;
                }
            }
        }
        String authority = null;
        String sridCode = null;
        if (srid != 0) {
            PreparedStatement ps = connection.prepareStatement("SELECT AUTH_NAME FROM PUBLIC.SPATIAL_REF_SYS "
                    + " WHERE SRID = ?");
            ps.setInt(1, srid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    authority = rs.getString(1);
                    sridCode = String.valueOf(srid);
                }
            } finally {
                ps.close();
            }
        }
        return new String[]{authority, sridCode};
    }

    /**
     * Alter a table to add a SRID constraint. The srid must be greater than
     * zero.
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

    /**
     * Return a SRID code from latitude and longitude coordinates
     *
     * @param connection to the database
     * @param latitude
     * @param longitude
     * @return a SRID code
     * @throws SQLException
     */
    public static int getSRID(Connection connection, float latitude, float longitude)
            throws SQLException {
        int srid = -1;
        PreparedStatement ps = connection.prepareStatement("select SRID from PUBLIC.SPATIAL_REF_SYS where PROJ4TEXT = ?");
        ps.setString(1, UTMUtils.getProj(latitude, longitude));
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                srid = rs.getInt(1);
            }
        } finally {
            ps.close();
        }
        return srid;
    }
}
