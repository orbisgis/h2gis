/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 *
 * Utilities to get geometry metadata from a table that contains at least one
 * geometry column
 *
 *
 * @author Erwan Bocher, CNRS (2020)
 */
public class GeometryTableUtilities {

    /**
     * Read the geometry metadata of the first geometry column
     *
     *
     * @param connection
     * @param geometryTable
     * @return Geometry MetaData
     * @throws java.sql.SQLException
     */
    public static Tuple<String, GeometryMetaData> getFirstColumnMetaData(Connection connection, TableLocation geometryTable) throws SQLException {
        try (ResultSet geomResultSet = getGeometryColumnsView(connection, geometryTable.getCatalog(), geometryTable.getSchema(),
                geometryTable.getTable())) {
            boolean isH2 = JDBCUtilities.isH2DataBase(connection);
            while (geomResultSet.next()) {
                String geometryColumnName = geomResultSet.getString("F_GEOMETRY_COLUMN");
                if (geometryColumnName != null && !geometryColumnName.isEmpty()) {
                    int dimension_ = geomResultSet.getInt("COORD_DIMENSION");
                    int srid_ = geomResultSet.getInt("SRID");
                    if (isH2) {
                        GeometryMetaData geometryMetaData = new GeometryMetaData();
                        geometryMetaData.setDimension(dimension_);
                        geometryMetaData.setGeometryTypeCode(geomResultSet.getInt("GEOMETRY_TYPE"));
                        geometryMetaData.setSRID(srid_);
                        geometryMetaData.initDimension();
                        geometryMetaData.initGeometryType();
                        return new Tuple<>(geometryColumnName, geometryMetaData);
                    } else {//POSTGIS case                    
                        return new Tuple<>(geometryColumnName, createMetadataFromPostGIS(geomResultSet.getString("type"), dimension_, srid_));
                    }
                }
            }
        }
        throw new SQLException(String.format("The table %s does not contain a geometry field", geometryTable));
    }

    /**
     * Read the geometry metadata of the first geometry column
     *
     *
     * @param resultSet
     * @return Geometry MetaData
     * @throws java.sql.SQLException
     */
    public static Tuple<String, GeometryMetaData> getFirstColumnMetaData(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metadata.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                GeometryMetaData geometryMetaData = GeometryMetaData.getMetaData(metadata.getColumnTypeName(i));
                return new Tuple<>(metadata.getColumnName(i), geometryMetaData);
            }
        }
        throw new SQLException(String.format("The query does not contain a geometry field"));
    }
     /**
     * Read the geometry metadata for a resulset
     *
     *
     * @param resultSet
     * @return Geometry MetaData
     * @throws java.sql.SQLException
     */
    public static LinkedHashMap<String, GeometryMetaData> getMetaData(ResultSet resultSet) throws SQLException {
            LinkedHashMap<String, GeometryMetaData> geometryMetaDatas = new LinkedHashMap<>();           
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metadata.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                GeometryMetaData geometryMetaData = GeometryMetaData.getMetaData(metadata.getColumnTypeName(i));
                geometryMetaDatas.put(metadata.getColumnName(i), geometryMetaData);
            }
        }
        return geometryMetaDatas;
    }

    /**
     * Read all geometry metadata from a table
     *
     * @param connection
     * @param geometryTable
     * @return Geometry MetaData
     * @throws java.sql.SQLException
     */
    public static LinkedHashMap<String, GeometryMetaData> getMetaData(Connection connection, TableLocation geometryTable) throws SQLException {
        try (ResultSet geomResultSet = getGeometryColumnsView(connection, geometryTable.getCatalog(), geometryTable.getSchema(),
                geometryTable.getTable())) {
            LinkedHashMap<String, GeometryMetaData> geometryMetaDatas = new LinkedHashMap<>();
            boolean isH2 = JDBCUtilities.isH2DataBase(connection);
            while (geomResultSet.next()) {
                String geometryColumnName = geomResultSet.getString("F_GEOMETRY_COLUMN");
                if (geometryColumnName != null && !geometryColumnName.isEmpty()) {
                    int dimension_ = geomResultSet.getInt("COORD_DIMENSION");
                    int srid_ = geomResultSet.getInt("SRID");
                    if (isH2) {
                        GeometryMetaData geometryMetaData = new GeometryMetaData();
                        geometryMetaData.setDimension(dimension_);
                        geometryMetaData.setGeometryTypeCode(geomResultSet.getInt("GEOMETRY_TYPE"));
                        geometryMetaData.setSRID(srid_);
                        geometryMetaData.initDimension();
                        geometryMetaData.initGeometryType();
                        geometryMetaDatas.put(geometryColumnName, geometryMetaData);
                    } else {//POSTGIS case                    
                        geometryMetaDatas.put(geometryColumnName, createMetadataFromPostGIS(geomResultSet.getString("type"), dimension_, srid_));
                    }
                }
            }
            return geometryMetaDatas;
        }
    }

    /**
     * Read the geometry metadata from a column name
     *
     * @param connection
     * @param geometryTable
     * @param geometryColumnName
     * @return Geometry MetaData
     * @throws java.sql.SQLException
     */
    public static GeometryMetaData getMetaData(Connection connection, TableLocation geometryTable, String geometryColumnName) throws SQLException {
        GeometryMetaData geometryMetaData = null;
        try (ResultSet geomResultSet = getGeometryColumnsView(connection, geometryTable.getCatalog(), geometryTable.getSchema(),
                geometryTable.getTable(), geometryColumnName)) {
            boolean isH2 = JDBCUtilities.isH2DataBase(connection);
            while (geomResultSet.next()) {
                if (geometryColumnName.isEmpty() || geomResultSet.getString("F_GEOMETRY_COLUMN").equalsIgnoreCase(geometryColumnName)) {
                    int dimension_ = geomResultSet.getInt("COORD_DIMENSION");
                    int srid_ = geomResultSet.getInt("SRID");
                    if (isH2) {
                        geometryMetaData = new GeometryMetaData();
                        geometryMetaData.setDimension(dimension_);
                        geometryMetaData.setGeometryTypeCode(geomResultSet.getInt("GEOMETRY_TYPE"));
                        geometryMetaData.setSRID(srid_);
                        geometryMetaData.initDimension();
                        geometryMetaData.initGeometryType();
                        break;
                    } else {//POSTGIS case                    
                        geometryMetaData = createMetadataFromPostGIS(geomResultSet.getString("type"), dimension_, srid_);
                        break;
                    }
                }
            }
        }
        return geometryMetaData;
    }

    /**
     * Find geometry metadata according the EWKT canonical form, its
     * coord_dimension and SRID
     *
     * This method is specific to POSTGIS because it doesn't store the full
     * representation of a geometry type e.g a POINTZ is stored with
     * coord_dimension= 3 and type = POINT a POINTZM is stored with
     * coord_dimension= 4 and type = POINT a POINTM is stored with
     * coord_dimension= 3 and type = POINTM
     *
     * as defined in SQL/MM specification. SQL-MM 3: 5.1.4 and OGC SFS 1.2
     *
     * @param type : geometry type
     * @param srid : srid value
     * @return GeometryMetaData
     */
    private static GeometryMetaData createMetadataFromPostGIS(String type, int coord_dimension, int srid) {
        GeometryMetaData geometryMetaData = new GeometryMetaData();
        geometryMetaData.setSRID(srid);
        if (type == null) {
            return geometryMetaData;
        }
        int geometry_code = GeometryTypeCodes.GEOMETRY;
        String sfs_geometry_type = "GEOMETRY";
        String geometry_type = "GEOMETRY";
        boolean hasz_ = false;
        boolean hasm_ = false;
        type = type.replaceAll(" ", "").replaceAll("\"", "");
        switch (type) {
            case "POINT":
                geometry_code = GeometryTypeCodes.POINT;
                sfs_geometry_type = "POINT";
                geometry_type = "POINT";
                if (coord_dimension > 3) {
                    hasz_ = true;
                    hasm_ = true;
                    geometry_type += "ZM";
                } else if (coord_dimension > 2) {
                    hasz_ = true;
                    geometry_type += "Z";
                }
                break;
            case "LINESTRING":
                geometry_code = GeometryTypeCodes.LINESTRING;
                sfs_geometry_type = "LINESTRING";
                geometry_type = "LINESTRING";
                if (coord_dimension > 3) {
                    hasz_ = true;
                    hasm_ = true;
                    geometry_type += "ZM";
                } else if (coord_dimension > 2) {
                    hasz_ = true;
                    geometry_type += "Z";
                }
                break;
            case "POLYGON":
                geometry_code = GeometryTypeCodes.POLYGON;
                sfs_geometry_type = "POLYGON";
                geometry_type = "POLYGON";
                if (coord_dimension > 3) {
                    hasz_ = true;
                    hasm_ = true;
                    geometry_type += "ZM";
                } else if (coord_dimension > 2) {
                    hasz_ = true;
                    geometry_type += "Z";
                }
                break;
            case "MULTIPOINT":
                geometry_code = GeometryTypeCodes.MULTIPOINT;
                sfs_geometry_type = "MULTIPOINT";
                geometry_type = "MULTIPOINT";
                if (coord_dimension > 3) {
                    hasz_ = true;
                    hasm_ = true;
                    geometry_type += "ZM";
                } else if (coord_dimension > 2) {
                    hasz_ = true;
                    geometry_type += "Z";
                }
                break;
            case "MULTILINESTRING":
                geometry_code = GeometryTypeCodes.MULTILINESTRING;
                sfs_geometry_type = "MULTILINESTRING";
                geometry_type = "MULTILINESTRING";
                if (coord_dimension > 3) {
                    hasz_ = true;
                    hasm_ = true;
                    geometry_type += "ZM";
                } else if (coord_dimension > 2) {
                    hasz_ = true;
                    geometry_type += "Z";
                }
                break;
            case "MULTIPOLYGON":
                geometry_code = GeometryTypeCodes.MULTIPOLYGON;
                sfs_geometry_type = "MULTIPOLYGON";
                geometry_type = "MULTIPOLYGON";
                if (coord_dimension > 3) {
                    hasz_ = true;
                    hasm_ = true;
                    geometry_type += "ZM";
                } else if (coord_dimension > 2) {
                    hasz_ = true;
                    geometry_type += "Z";
                }
                break;
            case "GEOMETRYCOLLECTION":
                geometry_code = GeometryTypeCodes.GEOMCOLLECTION;
                sfs_geometry_type = "GEOMCOLLECTION";
                geometry_type = "GEOMCOLLECTION";
                if (coord_dimension > 3) {
                    hasz_ = true;
                    hasm_ = true;
                    geometry_type += "ZM";
                } else if (coord_dimension > 2) {
                    hasz_ = true;
                    geometry_type += "Z";
                }
                break;
            case "POINTZ":
                geometry_code = GeometryTypeCodes.POINTZ;
                sfs_geometry_type = "POINTZ";
                geometry_type = "POINTZ";
                hasz_ = true;
                break;
            case "LINESTRINGZ":
                geometry_code = GeometryTypeCodes.LINESTRINGZ;
                sfs_geometry_type = "LINESTRINGZ";
                geometry_type = "LINESTRINGZ";
                hasz_ = true;
                break;
            case "POLYGONZ":
                geometry_code = GeometryTypeCodes.POLYGONZ;
                sfs_geometry_type = "POLYGONZ";
                geometry_type = "POLYGONZ";
                hasz_ = true;
                break;
            case "MULTIPOINTZ":
                geometry_code = GeometryTypeCodes.MULTIPOINTZ;
                sfs_geometry_type = "MULTIPOINTZ";
                geometry_type = "MULTIPOINTZ";
                hasz_ = true;
                break;
            case "MULTILINESTRINGZ":
                geometry_code = GeometryTypeCodes.MULTILINESTRINGZ;
                sfs_geometry_type = "MULTILINESTRINGZ";
                geometry_type = "MULTILINESTRINGZ";
                hasz_ = true;
                break;
            case "MULTIPOLYGONZ":
                geometry_code = GeometryTypeCodes.MULTIPOLYGONZ;
                sfs_geometry_type = "MULTIPOLYGONZ";
                geometry_type = "MULTIPOLYGONZ";
                hasz_ = true;
                break;
            case "GEOMETRYCOLLECTIONZ":
                geometry_code = GeometryTypeCodes.GEOMCOLLECTIONZ;
                sfs_geometry_type = "GEOMETRYCOLLECTIONZ";
                geometry_type = "GEOMETRYCOLLECTIONZ";
                hasz_ = true;
                break;
            case "POINTM":
                geometry_code = GeometryTypeCodes.POINTM;
                sfs_geometry_type = "POINTM";
                geometry_type = "POINTM";
                hasm_ = true;
                break;
            case "LINESTRINGM":
                geometry_code = GeometryTypeCodes.LINESTRINGM;
                sfs_geometry_type = "LINESTRINGM";
                geometry_type = "LINESTRINGM";
                hasm_ = true;
                break;
            case "POLYGONM":
                geometry_code = GeometryTypeCodes.POLYGONM;
                sfs_geometry_type = "POLYGONM";
                geometry_type = "POLYGONM";
                hasm_ = true;
                break;
            case "MULTIPOINTM":
                geometry_code = GeometryTypeCodes.MULTIPOINTM;
                sfs_geometry_type = "MULTIPOINTM";
                geometry_type = "MULTIPOINTM";
                hasm_ = true;
                break;
            case "MULTILINESTRINGM":
                geometry_code = GeometryTypeCodes.MULTILINESTRINGM;
                sfs_geometry_type = "MULTILINESTRINGM";
                geometry_type = "MULTILINESTRINGM";
                hasm_ = true;
                break;
            case "MULTIPOLYGONM":
                geometry_code = GeometryTypeCodes.MULTIPOLYGONM;
                sfs_geometry_type = "MULTIPOLYGONM";
                geometry_type = "MULTIPOLYGONM";
                hasm_ = true;
                break;
            case "GEOMETRYCOLLECTIONM":
                geometry_code = GeometryTypeCodes.GEOMCOLLECTIONM;
                sfs_geometry_type = "GEOMETRYCOLLECTIONM";
                geometry_type = "GEOMETRYCOLLECTIONM";
                hasm_ = true;
                break;
            case "POINTZM":
                geometry_code = GeometryTypeCodes.POINTZM;
                sfs_geometry_type = "POINTZM";
                geometry_type = "POINTZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "LINESTRINGZM":
                geometry_code = GeometryTypeCodes.LINESTRINGZM;
                sfs_geometry_type = "LINESTRINGZM";
                geometry_type = "LINESTRINGZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "POLYGONZM":
                geometry_code = GeometryTypeCodes.POLYGONZM;
                sfs_geometry_type = "POLYGONZM";
                geometry_type = "POLYGONZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "MULTIPOINTZM":
                geometry_code = GeometryTypeCodes.MULTIPOINTZM;
                sfs_geometry_type = "MULTIPOINTZM";
                geometry_type = "MULTIPOINTZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "MULTILINESTRINGZM":
                geometry_code = GeometryTypeCodes.MULTILINESTRINGZM;
                sfs_geometry_type = "MULTILINESTRINGZM";
                geometry_type = "MULTILINESTRINGZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "MULTIPOLYGONZM":
                geometry_code = GeometryTypeCodes.MULTIPOLYGONZM;
                sfs_geometry_type = "MULTIPOLYGONZM";
                geometry_type = "MULTIPOLYGONZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "GEOMETRYCOLLECTIONZM":
                geometry_code = GeometryTypeCodes.GEOMCOLLECTIONZM;
                sfs_geometry_type = "GEOMETRYCOLLECTIONZM";
                geometry_type = "GEOMETRYCOLLECTIONZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "GEOMETRY":
            default:
        }
        geometryMetaData.setDimension(coord_dimension);
        geometryMetaData.setGeometryTypeCode(geometry_code);
        geometryMetaData.setSfs_geometryType(sfs_geometry_type);
        geometryMetaData.setGeometryType(geometry_type);
        geometryMetaData.setHasM(hasm_);
        geometryMetaData.setHasZ(hasz_);
        return geometryMetaData;
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
     * Find the first geometry column name and its index of a resultSet.
     *
     *
     * @param resultSet ResultSet to analyse
     *
     * @return The name and index of first geometry field
     *
     * @throws SQLException
     */
    public static Tuple<String, Integer> getFirstGeometryColumnNameAndIndex(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (meta.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                return new Tuple<>(meta.getColumnName(i), i);
            }
        }
        throw new SQLException("The query doesn't contain any geometry field");
    }

    /**
     * Check if the ResultSet contains a geometry column
     *
     * @param resultSet ResultSet to analyse
     *
     * @return True if the ResultSet contains one geometry field
     *
     * @throws SQLException
     */
    public static boolean hasGeometryColumn(ResultSet resultSet) throws SQLException {
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
    public static Envelope getEnvelope(ResultSet resultSet) throws SQLException {
        return getEnvelope(resultSet, getFirstGeometryColumnNameAndIndex(resultSet).first());

    }

    /**
     * Compute the full extend of a ResultSet using a specified geometry column.
     * If the ResultSet does not contain this geometry field throw an exception
     *
     * @param resultSet ResultSet to analyse
     * @param geometryColumnName Field to analyse
     *
     * @return The full extend of the field in the ResultSet
     *
     * @throws SQLException
     */
    public static Envelope getEnvelope(ResultSet resultSet, String geometryColumnName) throws SQLException {
        Envelope aggregatedEnvelope = null;
        while (resultSet.next()) {
            Geometry geom = (Geometry) resultSet.getObject(geometryColumnName);
            if (aggregatedEnvelope != null) {
                aggregatedEnvelope.expandToInclude(geom.getEnvelopeInternal());
            } else {
                aggregatedEnvelope = geom.getEnvelopeInternal();
            }
        }
        return aggregatedEnvelope;
    }

    /**
     * Compute the 'estimated' extent of the given spatial table. Use the first
     * geometry field In case of POSTGIS : the estimated is taken from the
     * geometry column's statistics. In case of H2GIS : the estimated is taken
     * from the spatial index of the geometry column. If the estimated extend is
     * null the extent is computed.
     *
     * @param connection
     * @param tableLocation
     * @return
     * @throws java.sql.SQLException
     */
    public static Geometry getEstimatedExtent(Connection connection, TableLocation tableLocation) throws SQLException {
        LinkedHashMap<String, Integer> geometryFields = GeometryTableUtilities.getGeometryColumnNamesAndIndexes(connection, tableLocation);
        if (geometryFields.isEmpty()) {
            throw new SQLException("Cannot find any geometry column");
        }

        return getEstimatedExtent(connection, tableLocation, geometryFields.keySet().iterator().next());
    }

    /**
     * Compute the 'estimated' extent of the given spatial table. In case of
     * POSTGIS : the estimated is taken from the geometry column's statistics.
     * In case of H2GIS : the estimated is taken from the spatial index of the
     * geometry column. If the estimated extend is null the extent is computed.
     *
     * @param connection
     * @param tableLocation
     * @param geometryColumnName
     * @return
     * @throws java.sql.SQLException
     */
    public static Geometry getEstimatedExtent(Connection connection, TableLocation tableLocation, String geometryColumnName) throws SQLException {
        Geometry result;
        int srid = getSRID(connection, tableLocation, geometryColumnName);
        StringBuilder query = new StringBuilder("SELECT  ESTIMATED_ENVELOPE('");
        query.append(tableLocation.getTable()).append("','").append(geometryColumnName).append("')");
        try (ResultSet rs = connection.createStatement().executeQuery(query.toString())) {
            if (rs.next()) {
                result = (Geometry) rs.getObject(1);
                if (result != null) {
                    result.setSRID(srid);
                    return result;
                }
            }
        }
        query = new StringBuilder("SELECT  ENVELOPE(");
        query.append(TableLocation.quoteIdentifier(geometryColumnName)).append(") FROM ").append(tableLocation.getTable());
        try (ResultSet rsEnv = connection.createStatement().executeQuery(query.toString())) {
            if (rsEnv.next()) {
                result = (Geometry) rsEnv.getObject(1);
                if (result != null) {
                    result.setSRID(srid);
                    return result;
                }
            }
        }

        throw new SQLException("Unable to compute the estimated extent");
    }

    /**
     * Return the SRID of the first geometry column of the input table
     *
     * @param connection Active connection
     * @param tableLocation Table name
     * @param geometryColumnName The geometryf field column
     *
     * @return The SRID of the first geometry column
     *
     * @throws SQLException
     */
    public static int getSRID(Connection connection, TableLocation tableLocation, String geometryColumnName) throws SQLException {
        int srid = 0;
        try (ResultSet geomResultSet = getGeometryColumnsView(connection, tableLocation.getCatalog(), tableLocation.getSchema(),
                tableLocation.getTable(), geometryColumnName)) {
            srid = 0;
            while (geomResultSet.next()) {
                srid = geomResultSet.getInt("srid");
                break;
            }
        }
        return srid;
    }

    /**
     * Return the SRID of the first geometry column of the input table
     *
     * @param connection Active connection
     * @param tableLocation Table name
     *
     * @return The SRID of the first geometry column
     *
     * @throws SQLException
     */
    public static int getSRID(Connection connection, TableLocation tableLocation) throws SQLException {
        int srid = 0;
        try (ResultSet geomResultSet = getGeometryColumnsView(connection, tableLocation.getCatalog(), tableLocation.getSchema(),
                tableLocation.getTable())) {
            srid = 0;
            while (geomResultSet.next()) {
                srid = geomResultSet.getInt("srid");
                break;
            }
        }
        return srid;
    }

    /**
     * Find geometry column names and indexes of a table
     *
     * @param connection Active connection
     * @param tableLocation Table location
     *
     * @return A list of Geometry column names and indexes
     *
     * @throws SQLException
     */
    public static LinkedHashMap<String, Integer> getGeometryColumnNamesAndIndexes(Connection connection, TableLocation tableLocation) throws SQLException {
        try (ResultSet resultSet = connection.createStatement().executeQuery(
                "SELECT * FROM " + tableLocation + " WHERE 1=0;")) {
            return getGeometryColumnNamesAndIndexes(resultSet.getMetaData());
        }
    }

    /**
     * Find geometry column names and indexes from a resulset
     *
     * @param metadata metadata of a resulset
     * @return A list of Geometry column names and indexes
     *
     * @throws SQLException
     */
    public static LinkedHashMap<String, Integer> getGeometryColumnNamesAndIndexes(ResultSetMetaData metadata) throws SQLException {
        LinkedHashMap<String, Integer> namesWithIndexes = new LinkedHashMap<>();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metadata.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                namesWithIndexes.put(metadata.getColumnName(i), i);
            }
        }
        return namesWithIndexes;
    }

    /**
     * Find geometry column names
     *
     * @param connection Active connection
     * @param tableLocation Table location
     *
     * @return A list of Geometry column names and indexes
     *
     * @throws SQLException
     */
    public static List<String> getGeometryColumnNames(Connection connection, TableLocation tableLocation) throws SQLException {
        try (ResultSet resultSet = connection.createStatement().executeQuery(
                "SELECT * FROM " + tableLocation + " WHERE 1=0;")) {
            return getGeometryColumnNames(resultSet.getMetaData());
        }
    }

    /**
     * Find geometry column names from a resulset
     *
     * @param metadata metadata of a resulset
     * @return A list of Geometry column names
     *
     * @throws SQLException
     */
    public static List<String> getGeometryColumnNames(ResultSetMetaData metadata) throws SQLException {
        ArrayList<String> namesWithIndexes = new ArrayList<>();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metadata.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                namesWithIndexes.add(metadata.getColumnName(i));
            }
        }
        return namesWithIndexes;
    }

    /**
     * Find the first geometry column name of a table with its index
     *
     * @param connection Active connection
     * @param tableLocation Table location
     * @return The first geometry column name and its index
     *
     * @throws SQLException
     */
    public static Tuple<String, Integer> getFirstGeometryColumnNameAndIndex(Connection connection, TableLocation tableLocation) throws SQLException {
        Statement statement = connection.createStatement();
        try (ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM " + tableLocation + " WHERE 1=0;")) {
            return GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(resultSet.getMetaData());
        }
    }

    /**
     * Find the first geometry column name of a table with its index
     *
     * @param metadata metadata of a resulset
     * @return The first geometry column name and its index
     *
     * @throws SQLException
     */
    public static Tuple<String, Integer> getFirstGeometryColumnNameAndIndex(ResultSetMetaData metadata) throws SQLException {
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metadata.getColumnTypeName(i).equalsIgnoreCase("geometry")) {
                return new Tuple<>(metadata.getColumnName(i), i);
            }
        }
        throw new SQLException("The query doesn't contain any geometry field");
    }

    /**
     *
     * @param connection Active connection
     * @param catalog
     * @param schema
     * @param table
     * @return
     * @throws SQLException
     */
    public static ResultSet getGeometryColumnsView(Connection connection, String catalog, String schema, String table)
            throws SQLException {
        PreparedStatement geomStatement = prepareInformationSchemaStatement(connection, catalog, schema, table,
                "geometry_columns", "");
        return geomStatement.executeQuery();
    }

    /**
     * Return a resulset of the geometry column view properties from
     *
     * @param connection
     * @param catalog
     * @param schema
     * @param table
     * @param geometryField
     * @return
     * @throws SQLException
     */
    public static ResultSet getGeometryColumnsView(Connection connection, String catalog, String schema, String table, String geometryField)
            throws SQLException {
        if (geometryField != null && !geometryField.isEmpty()) {
            PreparedStatement geomStatement = prepareInformationSchemaStatement(connection, catalog, schema, table,
                    "geometry_columns", String.format(" and F_GEOMETRY_COLUMN ='%s'", geometryField));
            return geomStatement.executeQuery();
        }
        throw new SQLException("Unable to compute the estimated extent");
    }

    /**
     * Merge the bounding box of all geometries inside the provided table.
     *
     * @param connection Active connection (not closed by this function)
     * @param location Location of the table
     * @param geometryField Geometry field or empty string (take the first
     * geometry field)
     *
     * @return Envelope of the table
     *
     * @throws SQLException If the table not exists, empty or geometry field
     * empty.
     */
    public static Envelope getEnvelope(Connection connection, TableLocation location, String geometryField)
            throws SQLException {
        if (geometryField == null || geometryField.isEmpty()) {
            throw new SQLException("The table " + location + " does not contain a Geometry field, then the extent "
                    + "cannot be computed");
        }
        boolean isH2 = JDBCUtilities.isH2DataBase(connection);
        if (isH2) {
            try (ResultSet rs = connection.createStatement().executeQuery("SELECT ST_Extent("
                    + TableLocation.quoteIdentifier(geometryField) + ") as ext FROM " + location)) {
                if (rs.next()) {
                    return ((Geometry) rs.getObject(1)).getEnvelopeInternal();
                }
            }
        } else {
            try (ResultSet rs = connection.createStatement().executeQuery("SELECT ST_SetSRID(ST_Extent("
                    + TableLocation.quoteIdentifier(geometryField) + "), ST_SRID(" + TableLocation.quoteIdentifier(geometryField) + ")) as ext FROM " + location)) {
                if (rs.next()) {
                    return ((Geometry) rs.getObject(1)).getEnvelopeInternal();
                }
            }
        }
        throw new SQLException("Unable to get the table extent it may be empty");
    }

    /**
     * Merge the bounding box of all geometries inside the provided table.
     *
     * Use the first geometry column
     *
     * @param connection Active connection (not closed by this function)
     * @param location Location of the table
     *
     * @return Envelope of the table
     *
     * @throws SQLException If the table not exists, empty or does not contain a
     * geometry field.
     */
    public static Envelope getEnvelope(Connection connection, TableLocation location)
            throws SQLException {
        LinkedHashMap<String, Integer> geometryFields = GeometryTableUtilities.getGeometryColumnNamesAndIndexes(connection, location);
        if (geometryFields.isEmpty()) {
            throw new SQLException("The table " + location + " does not contain a Geometry field, then the extent "
                    + "cannot be computed");
        }
        return getEnvelope(connection, location, geometryFields.keySet().stream().findFirst().get());
    }

    /**
     * Return an array of two string that correspond to the authority name and
     * its SRID code. If the SRID does not exist return the array {null, null}
     *
     * @param connection Active connection
     * @param table Table name
     * @param geometryColumnName Field to analyse
     *
     * @return Array of two string that correspond to the authority name and its
     * SRID code
     *
     * @throws SQLException
     */
    public static String[] getAuthorityAndSRID(Connection connection, TableLocation table, String geometryColumnName)
            throws SQLException {
        int srid = getSRID(connection, table, geometryColumnName);
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

}
