/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.h2gis.utilities.dbtypes.DBTypes.*;
import static org.h2gis.utilities.dbtypes.DBUtils.getDBType;

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
     * @param connection database connection
     * @param geometryTable table name
     * @return Geometry MetaData
     */
    public static Tuple<String, GeometryMetaData> getFirstColumnMetaData(Connection connection, String geometryTable) throws SQLException {
        return getFirstColumnMetaData(connection, TableLocation.parse(geometryTable, getDBType(connection)));
    }

    /**
     * Read the geometry metadata of the first geometry column
     *
     *
     * @param connection database connection
     * @param geometryTable table name
     * @return Geometry MetaData
     */
    public static Tuple<String, GeometryMetaData> getFirstColumnMetaData(Connection connection, TableLocation geometryTable) throws SQLException {
        DBTypes dbTypes = geometryTable.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes== H2|| dbTypes== POSTGRESQL) {
            try (ResultSet geomResultSet = getGeometryColumnsView(connection, geometryTable.getCatalog(), geometryTable.getSchema(),
                geometryTable.getTable())) {
            while (geomResultSet.next()) {
                String geometryColumnName = geomResultSet.getString("F_GEOMETRY_COLUMN");
                if (geometryColumnName != null && !geometryColumnName.isEmpty()) {
                    int dimension_ = geomResultSet.getInt("COORD_DIMENSION");
                    int srid_ = geomResultSet.getInt("SRID");
                    if (dbTypes==H2||dbTypes==H2GIS) {
                        GeometryMetaData geometryMetaData = new GeometryMetaData();
                        geometryMetaData.setDimension(dimension_);
                        geometryMetaData.setGeometryTypeCode(geomResultSet.getInt("GEOMETRY_TYPE"));
                        geometryMetaData.setSRID(srid_);
                        geometryMetaData.initDimension();
                        geometryMetaData.initGeometryType();
                        return new Tuple<>(geometryColumnName, geometryMetaData);
                    } else  {//POSTGIS case
                        return new Tuple<>(geometryColumnName, createMetadataFromPostGIS(geomResultSet.getString("type"), dimension_, srid_));
                    }
                }
            }
        }
        throw new SQLException(String.format("The table %s does not contain a geometry field", geometryTable));
        }
        throw new SQLException("Database not supported");
    }

    /**
     * The geometry metadata of a resulset is getting from the first row Because
     * a resulset can mixed geometry types and SRID we are not able to return
     * all geometry metadatas
     *
     * Use this method only to instantiate a GeometryMetaData object
     *
     * @param resultSet active resultset
     * @return Partial geometry metaData
     */
    public static Tuple<String, GeometryMetaData> getFirstColumnMetaData(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            GeometryMetaData geomMeta = GeometryMetaData.getMetaDataFromTablePattern(metadata.getColumnTypeName(i));
            if (geomMeta != null) {
                return new Tuple<>(metadata.getColumnName(i), geomMeta);
            }
        }
        throw new SQLException("The query does not contain a geometry field");
    }

    /**
     * Read the geometry metadata for a resulset
     *
     *
     * @param resultSet active resultset
     * @return Geometry MetaData
     */
    public static LinkedHashMap<String, GeometryMetaData> getMetaData(ResultSet resultSet) throws SQLException {
        LinkedHashMap<String, GeometryMetaData> geometryMetaDatas = new LinkedHashMap<>();
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            GeometryMetaData geomMeta = GeometryMetaData.getMetaDataFromTablePattern(metadata.getColumnTypeName(i));
            if (geomMeta != null) {
                geometryMetaDatas.put(metadata.getColumnName(i), geomMeta);
            }
        }
        return geometryMetaDatas;
    }
    
    /**
     * Read all geometry metadata from a table
     *
     * @param connection database connection
     * @param geometryTable geometry table name
     * @return Geometry MetaData
     */
    public static LinkedHashMap<String, GeometryMetaData> getMetaData(Connection connection, String geometryTable) throws SQLException {
            return getMetaData(connection, TableLocation.parse(geometryTable, getDBType(connection)));
    }

    /**
     * Read all geometry metadata from a table
     *
     * @param connection database connection
     * @param geometryTable geometry table name
     * @return Geometry MetaData
     */
    public static LinkedHashMap<String, GeometryMetaData> getMetaData(Connection connection, TableLocation geometryTable) throws SQLException {
        DBTypes dbTypes = geometryTable.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes== H2|| dbTypes== POSTGRESQL) {
            try (ResultSet geomResultSet = getGeometryColumnsView(connection, geometryTable.getCatalog(), geometryTable.getSchema(),
                    geometryTable.getTable())) {
                LinkedHashMap<String, GeometryMetaData> geometryMetaDatas = new LinkedHashMap<>();
                while (geomResultSet.next()) {
                    String geometryColumnName = geomResultSet.getString("F_GEOMETRY_COLUMN");
                    if (geometryColumnName != null && !geometryColumnName.isEmpty()) {
                        int dimension_ = geomResultSet.getInt("COORD_DIMENSION");
                        int srid_ = geomResultSet.getInt("SRID");
                        if (dbTypes == H2GIS || dbTypes==H2) {
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
        throw new SQLException("Database not supported");
    }

    /**
     * Read the geometry metadata from a column name
     *
     * @param connection database connection
     * @param geometryTable geometry table name
     * @param geometryColumnName geometry column name
     * @return Geometry MetaData
     */
    public static GeometryMetaData getMetaData(Connection connection, String geometryTable, String geometryColumnName) throws SQLException {
        return getMetaData(connection, TableLocation.parse(geometryTable, getDBType(connection)), geometryColumnName);
    }

    /**
     * Read the geometry metadata from a column name
     *
     * @param connection database connection
     * @param geometryTable geometry table name
     * @param geometryColumnName geometry column name
     * @return Geometry MetaData
     */
    public static GeometryMetaData getMetaData(Connection connection, TableLocation geometryTable, String geometryColumnName) throws SQLException {
        GeometryMetaData geometryMetaData = null;
        final DBTypes dbTypes = geometryTable.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== H2) {
            try (ResultSet geomResultSet = getGeometryColumnsView(connection, geometryTable.getCatalog(), geometryTable.getSchema("PUBLIC"),
                    geometryTable.getTable(), TableLocation.quoteIdentifier(geometryColumnName, dbTypes))) {
                while (geomResultSet.next()) {
                    if (geometryColumnName.isEmpty() || geomResultSet.getString("F_GEOMETRY_COLUMN").equalsIgnoreCase(geometryColumnName)) {
                        int dimension_ = geomResultSet.getInt("COORD_DIMENSION");
                        int srid_ = geomResultSet.getInt("SRID");
                        geometryMetaData = new GeometryMetaData();
                        geometryMetaData.setDimension(dimension_);
                        geometryMetaData.setGeometryTypeCode(geomResultSet.getInt("GEOMETRY_TYPE"));
                        geometryMetaData.setSRID(srid_);
                        geometryMetaData.initDimension();
                        geometryMetaData.initGeometryType();
                        break;
                    }
                }
            }
            return geometryMetaData;
        }else if(dbTypes== POSTGRESQL|| dbTypes== POSTGIS ){
            try (ResultSet geomResultSet = getGeometryColumnsView(connection, geometryTable.getCatalog(), geometryTable.getSchema("public"),
                    geometryTable.getTable(), TableLocation.quoteIdentifier(geometryColumnName, dbTypes))) {
                while (geomResultSet.next()) {
                    if (geometryColumnName.isEmpty() || geomResultSet.getString("F_GEOMETRY_COLUMN").equalsIgnoreCase(geometryColumnName)) {
                        int dimension_ = geomResultSet.getInt("COORD_DIMENSION");
                        int srid_ = geomResultSet.getInt("SRID");
                        geometryMetaData = createMetadataFromPostGIS(geomResultSet.getString("type"), dimension_, srid_);
                        break;
                    }
                }
            }
            return geometryMetaData;
        }
        throw new SQLException("Database not supported");
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
     */
    public static Tuple<String, Integer> getFirstGeometryColumnNameAndIndex(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (meta.getColumnTypeName(i).toLowerCase().startsWith("geometry")) {
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
     */
    public static boolean hasGeometryColumn(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (meta.getColumnTypeName(i).toLowerCase().startsWith("geometry")) {
                return true;
            }
        }
        return false;
    }
    
     /**
     * Check if the table contains a geometry column
     *
     * @param connection database connection
     * @param tableLocation input table name
     *
     * @return True if the ResultSet contains one geometry field
     *
     */
    public static boolean hasGeometryColumn(Connection connection, String tableLocation) throws SQLException {
        return hasGeometryColumn(connection, TableLocation.parse(tableLocation, getDBType(connection)));
    }

    /**
     * Check if the table contains a geometry column
     *
     * @param connection database connection
     * @param tableLocation input table name
     *
     * @return True if the ResultSet contains one geometry field
     *
     */
    public static boolean hasGeometryColumn(Connection connection, TableLocation tableLocation) throws SQLException {
        Statement statement = connection.createStatement();
        DBTypes dbTypes = tableLocation.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes==H2 || dbTypes==POSTGRESQL) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableLocation.toString() + " WHERE 1=0;")) {
                ResultSetMetaData meta = resultSet.getMetaData();
                int columnCount = meta.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    if (meta.getColumnTypeName(i).toLowerCase().startsWith("geometry")) {
                        return true;
                    }
                }
            }
            return false;
        }
        throw  new SQLException("Database not supported");
    }

    /**
     * Compute the full extend of a ResultSet using the first geometry field. If
     * the ResultSet does not contain any geometry field throw an exception
     *
     * @param resultSet ResultSet to analyse
     *
     * @return A geometry that represents the full extend of the first geometry
     * column in the ResultSet
     *
     */
    public static Geometry getEnvelope(ResultSet resultSet) throws SQLException {
        return getEnvelope(resultSet, getFirstGeometryColumnNameAndIndex(resultSet).first());

    }

    /**
     * Compute the full extend of a ResultSet using a specified geometry column.
     * If the ResultSet does not contain this geometry field throw an exception
     * If the geometries don't have the same SRID throw an exception
     *
     * @param resultSet ResultSet to analyse
     * @param geometryColumnName Field to analyse
     *
     * @return The full extend of the geometry column name in the ResultSet
     *
     */
    public static Geometry getEnvelope(ResultSet resultSet, String geometryColumnName) throws SQLException {
        //First one
        resultSet.next();
        Geometry geom = (Geometry) resultSet.getObject(geometryColumnName);
        int firstSRID = geom.getSRID();
        Envelope aggregatedEnvelope  = geom.getEnvelopeInternal();
        if(aggregatedEnvelope==null){
            aggregatedEnvelope = new Envelope();
        }
        //Next
        while (resultSet.next()) {
            geom = (Geometry) resultSet.getObject(geometryColumnName);
            if (geom.getSRID() != firstSRID) {
                throw new SQLException("The envelope cannot be computed on mixed SRID");
            }
            aggregatedEnvelope.expandToInclude(geom.getEnvelopeInternal());
        }
        Geometry geomEnv = new GeometryFactory().toGeometry(aggregatedEnvelope);
        geomEnv.setSRID(firstSRID);
        return geomEnv;
    }
    
     /**
     * Compute the 'estimated' extent of the given spatial table. Use the first
     * geometry field In case of POSTGIS : the estimated is taken from the
     * geometry column's statistics. In case of H2GIS : the estimated is taken
     * from the spatial index of the geometry column. If the estimated extent is
     * null the extent is computed.
     *
     * @param connection database
     * @param tableName table name
     * @return the 'estimated' extent of the given spatial table.
     */
    public static Geometry getEstimatedExtent(Connection connection, String tableName) throws SQLException {
        return getEstimatedExtent(connection, TableLocation.parse(tableName, DBUtils.getDBType(connection)));
    }

    /**
     * Compute the 'estimated' extent of the given spatial table. Use the first
     * geometry field In case of POSTGIS : the estimated is taken from the
     * geometry column's statistics. In case of H2GIS : the estimated is taken
     * from the spatial index of the geometry column. If the estimated extent is
     * null the extent is computed.
     *
     * @param connection database
     * @param tableLocation table name
     * @return an estimated extent of the table as geometry
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
     * geometry column. If the estimated extent is null the extent is computed.
     *
     * @param connection database
     * @param tableName table name
     * @param geometryColumnName geometry column name
     * @return an estimated extent of the table as geometry
     */
    public static Geometry getEstimatedExtent(Connection connection, String tableName, String geometryColumnName) throws SQLException {
        return getEstimatedExtent(connection, TableLocation.parse(tableName, DBUtils.getDBType(connection)), geometryColumnName);
    }

    /**
     * Compute the 'estimated' extent of the given spatial table. In case of
     * POSTGIS : the estimated is taken from the geometry column's statistics.
     * In case of H2GIS : the estimated is taken from the spatial index of the
     * geometry column. If the estimated extent is null the extent is computed.
     *
     * @param connection database
     * @param tableLocation table name
     * @param geometryColumnName geometry column name
     * @return  an estimated extent of the table as geometry
     */
    public static Geometry getEstimatedExtent(Connection connection, TableLocation tableLocation, String geometryColumnName) throws SQLException {
        DBTypes dbTypes = tableLocation.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes==H2 || dbTypes==POSTGRESQL) {
        Geometry result;
        int srid = getSRID(connection, tableLocation, geometryColumnName);
        if (dbTypes==POSTGIS || dbTypes==POSTGRESQL) {
            StringBuilder query = new StringBuilder("SELECT  ST_EstimatedExtent(");
            if (!tableLocation.getSchema().isEmpty()) {
                query.append("'").append(tableLocation.getSchema()).append("',");
            }
            else{
                query.append("'").append("public").append("',");
            }
            query.append("'").append(tableLocation.getTable()).append("','").append(geometryColumnName).append("') :: geometry");
            try (ResultSet rs = connection.createStatement().executeQuery(query.toString())) {
                if (rs.next()) {
                    result = ((Geometry) rs.getObject(1));
                    if (result != null) {
                        result.setSRID(srid);
                    }
                    return result;
                }
            }
        } else {
            StringBuilder query = new StringBuilder("SELECT  ESTIMATED_ENVELOPE('");
            query.append(tableLocation.toString()).append("','").append(TableLocation.capsIdentifier(geometryColumnName, H2GIS)).append("')");
            try (ResultSet rs = connection.createStatement().executeQuery(query.toString())) {
                if (rs.next()) {
                    result = (Geometry) rs.getObject(1);
                    if (result != null) {
                        result.setSRID(srid);
                        return result;
                    }
                }
            }
            query = new StringBuilder("SELECT ENVELOPE(");
            query.append(TableLocation.capsIdentifier(geometryColumnName, H2GIS)).append(") FROM ").append(tableLocation.toString(H2GIS));
            try (ResultSet rsEnv = connection.createStatement().executeQuery(query.toString())) {
                if (rsEnv.next()) {
                    result = (Geometry) rsEnv.getObject(1);
                    if (result != null) {
                        result.setSRID(srid);
                        return result;
                    }
                }
            }
        }
        throw new SQLException("Unable to compute the estimated extent");
        }
        throw  new SQLException("Database not supported");
    }

     /**
     * Return the SRID of the first geometry column of the input table
     *
     * @param connection Active connection
     * @param tableName Table name
     * @param geometryColumnName The geometryf field column
     *
     * @return The SRID of the first geometry column
     *
     */
    public static int getSRID(Connection connection,String tableName, String geometryColumnName) throws SQLException {
     return getSRID(connection, TableLocation.parse(tableName, DBUtils.getDBType(connection)), geometryColumnName);
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
     */
    public static int getSRID(Connection connection, TableLocation tableLocation, String geometryColumnName) throws SQLException {
        int srid = 0;
        String columnName = TableLocation.capsIdentifier(geometryColumnName, tableLocation.getDbTypes());
        try (ResultSet geomResultSet = getGeometryColumnsView(connection, tableLocation.getCatalog(), tableLocation.getSchema("PUBLIC"),
                tableLocation.getTable(), columnName)) {
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
     * @param tableName Table name
     *
     * @return The SRID of the first geometry column
     *
     */
    public static int getSRID(Connection connection, String tableName) throws SQLException {
        return getSRID(connection, TableLocation.parse(tableName, DBUtils.getDBType(connection)));
    }

    /**
     * Return the SRID of the first geometry column of the input table
     *
     * @param connection Active connection
     * @param tableLocation Table name
     *
     * @return The SRID of the first geometry column
     *
     */
    public static int getSRID(Connection connection, TableLocation tableLocation) throws SQLException {
        int srid = 0;
        try (ResultSet geomResultSet = getGeometryColumnsView(connection, tableLocation.getCatalog(), tableLocation.getSchema("PUBLIC"),
                tableLocation.getTable())) {
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
     * @param tableName Table location
     *
     * @return A list of Geometry column names and indexes
     *
     **/
    public static LinkedHashMap<String, Integer> getGeometryColumnNamesAndIndexes(Connection connection, String tableName) throws SQLException {
        return getGeometryColumnNamesAndIndexes(connection, TableLocation.parse(tableName, DBUtils.getDBType(connection)));
    }
    /**
     * Find geometry column names and indexes of a table
     *
     * @param connection Active connection
     * @param tableLocation Table location
     *
     * @return A list of Geometry column names and indexes
     *
     */
    public static LinkedHashMap<String, Integer> getGeometryColumnNamesAndIndexes(Connection connection, TableLocation tableLocation) throws SQLException {
        DBTypes dbTypes = tableLocation.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes==H2 || dbTypes==POSTGRESQL) {
            try (ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT * FROM " + tableLocation + " WHERE 1=0;")) {
                return getGeometryColumnNamesAndIndexes(resultSet.getMetaData());
            }
        }
        throw new SQLException("Database not supported");
    }

    /**
     * Find geometry column names and indexes from a resulset
     *
     * @param metadata metadata of a resulset
     * @return A list of Geometry column names and indexes
     *
     */
    public static LinkedHashMap<String, Integer> getGeometryColumnNamesAndIndexes(ResultSetMetaData metadata) throws SQLException {
        LinkedHashMap<String, Integer> namesWithIndexes = new LinkedHashMap<>();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metadata.getColumnTypeName(i).toLowerCase().startsWith("geometry")) {
                namesWithIndexes.put(metadata.getColumnName(i), i);
            }
        }
        return namesWithIndexes;
    }

     /**
     * Find geometry column names
     *
     * @param connection Active connection
     * @param tableName Table location
     *
     * @return A list of Geometry column names and indexes
     *
     */
    public static List<String> getGeometryColumnNames(Connection connection, String tableName) throws SQLException {
     return getGeometryColumnNames(connection, TableLocation.parse(tableName, DBUtils.getDBType(connection)));
    }

    /**
     * Find geometry column names
     *
     * @param connection Active connection
     * @param tableLocation Table location
     *
     * @return A list of Geometry column names and indexes
     *
     */
    public static List<String> getGeometryColumnNames(Connection connection, TableLocation tableLocation) throws SQLException {
        DBTypes dbTypes = tableLocation.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes==H2 || dbTypes==POSTGRESQL) {
            try (ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT * FROM " + tableLocation + " WHERE 1=0;")) {
                return getGeometryColumnNames(resultSet.getMetaData());
            }
        }
        throw new SQLException("Database not supported");
    }

    /**
     * Find geometry column names from a resulset
     *
     * @param metadata metadata of a resulset
     * @return A list of Geometry column names
     *
     */
    public static List<String> getGeometryColumnNames(ResultSetMetaData metadata) throws SQLException {
        ArrayList<String> namesWithIndexes = new ArrayList<>();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metadata.getColumnTypeName(i).toLowerCase().startsWith("geometry")) {
                namesWithIndexes.add(metadata.getColumnName(i));
            }
        }
        return namesWithIndexes;
    }
    
     /**
     * Find the first geometry column name of a table with its index
     *
     * @param connection Active connection
     * @param tableName Table location
     * @return The first geometry column name and its index
     *
     */
    public static Tuple<String, Integer> getFirstGeometryColumnNameAndIndex(Connection connection, String tableName) throws SQLException {
        return getFirstGeometryColumnNameAndIndex(connection, TableLocation.parse(tableName, DBUtils.getDBType(connection)));
    }

    /**
     * Find the first geometry column name of a table with its index
     *
     * @param connection Active connection
     * @param tableLocation Table location
     * @return The first geometry column name and its index
     *
     */
    public static Tuple<String, Integer> getFirstGeometryColumnNameAndIndex(Connection connection, TableLocation tableLocation) throws SQLException {
        DBTypes dbTypes = tableLocation.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes== H2|| dbTypes== POSTGRESQL) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableLocation + " WHERE 1=0;")) {
                return GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(resultSet.getMetaData());
            }
        }
        throw new SQLException("Database not supported");
    }

    /**
     * Find the first geometry column name of a table with its index
     *
     * @param metadata metadata of a resulset
     * @return The first geometry column name and its index
     *
     */
    public static Tuple<String, Integer> getFirstGeometryColumnNameAndIndex(ResultSetMetaData metadata) throws SQLException {
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
                if (metadata.getColumnTypeName(i).toLowerCase().startsWith("geometry")) {
                    return new Tuple<>(metadata.getColumnName(i), i);
                }
        }
        throw new SQLException("The query doesn't contain any geometry field");
    }

    /**
     * Return a resulset of the geometry column view properties from
     *
     * @param connection Active connection
     * @param catalog catalog name
     * @param schema schema name
     * @param table table name
     * @return ResultSet of the geometry column view
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
     * @param connection Active connection
     * @param catalog catalog name
     * @param schema schema name
     * @param table table name
     * @param geometryField geometry column name
     * @return ResultSet of the geometry column view
     */
    public static ResultSet getGeometryColumnsView(Connection connection, String catalog, String schema, String table, String geometryField)
            throws SQLException {
        if (geometryField != null && !geometryField.isEmpty()) {
            PreparedStatement geomStatement = prepareInformationSchemaStatement(connection, catalog, schema, table,
                    "geometry_columns", String.format(" and F_GEOMETRY_COLUMN ='%s'", geometryField));
            return geomStatement.executeQuery();
        }
        throw new SQLException("Unable to get geometry metadata from a null or empty column name");
    }
    /**
     * Merge the bounding box of all geometries inside the provided table.
     *
     * @param connection Active connection (not closed by this function)
     * @param table Location of the table
     * @param geometryColumn Geometry field or empty string (take the first
     * geometry field)
     *
     * @return Envelope of the table as Geometry
     *
     * @throws SQLException If the table not exists, empty or geometry field
     * empty.
     */
    public static Geometry getEnvelope(Connection connection, String table, String geometryColumn)
            throws SQLException {
        return getEnvelope(connection, TableLocation.parse(geometryColumn, DBUtils.getDBType(connection)), geometryColumn);
    }

    /**
     * Merge the bounding box of all geometries inside the provided table.
     *
     * @param connection Active connection (not closed by this function)
     * @param location Location of the table
     * @param geometryColumn Geometry field or empty string (take the first
     * geometry field)
     *
     * @return Envelope of the table as Geometry
     *
     * @throws SQLException If the table not exists, empty or geometry field
     * empty.
     */
    public static Geometry getEnvelope(Connection connection, TableLocation location, String geometryColumn)
            throws SQLException {
        DBTypes dbTypes = location.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes==H2 || dbTypes==POSTGRESQL) {
            if (geometryColumn == null || geometryColumn.isEmpty()) {
                throw new SQLException("The table " + location + " does not contain a Geometry field, then the extent "
                        + "cannot be computed");
            }
            if (dbTypes== H2GIS || dbTypes==H2) {
                try (ResultSet rs = connection.createStatement().executeQuery("SELECT ST_Extent("
                        + TableLocation.quoteIdentifier(geometryColumn) + ") as ext FROM " + location)) {
                    if (rs.next()) {
                        return ((Geometry) rs.getObject(1));
                    }
                }
            } else  {
                try (ResultSet rs = connection.createStatement().executeQuery("SELECT ST_SetSRID(ST_Extent("
                        + TableLocation.quoteIdentifier(geometryColumn) + "), MAX(ST_SRID(" + TableLocation.quoteIdentifier(geometryColumn) + "))) as ext FROM " + location)) {
                    if (rs.next()) {
                        return ((Geometry) rs.getObject(1));
                    }
                }
            }
            throw new SQLException("Unable to get the table extent it may be empty");
        }
        throw new SQLException("Database not supported");
    }
    /**
     *
     * Merge the bounding box of all geometries inside the provided table and
     * geometry columns
     *
     * Note that the geometry column can be an expression.
     *
     *
     * Supported syntaxes
     * {@code
     * the_geom -> Column name st_buffer(the_geom, 20) ->
     * Geometry function
     * }
     *
     * @param connection Active connection (not closed by this function)
     * @param location Location of the table
     * @param geometryColumns List of geometry columns or geometry functions
     *
     * @return Envelope of the table as Geometry
     *
     * @throws SQLException If the table not exists, empty or geometry field
     * empty.
     *
     */
    public static Geometry getEnvelope(Connection connection, TableLocation location, String... geometryColumns)
            throws SQLException {
        return getEnvelope(connection, location, geometryColumns, null);
    }

    /**
     *
     * Merge the bounding box of all geometries inside the provided table,
     * geometry columns and filter condition
     *
     * Note that the geometry column can be an expression.
     *
     * Supported syntaxes
     * {@code
     * the_geom -> Column name st_buffer(the_geom, 20) ->
     * Geometry function
     * }
     *
     *
     * @param connection Active connection (not closed by this function)
     * @param location Location of the table
     * @param geometryColumns List of geometry columns or geometry functions
     * @param filter filter condition after the from
     *
     * @return Envelope of the table as Geometry
     *
     * @throws SQLException If the table not exists, empty or geometry field
     * empty.
     *
     */
    public static Geometry getEnvelope(Connection connection, TableLocation location, String[] geometryColumns, String filter)
            throws SQLException {
        DBTypes dbTypes = location.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes==H2 || dbTypes==POSTGRESQL) {
            if (geometryColumns == null || geometryColumns.length == 0) {
                throw new SQLException("The table " + location + " does not contain a geometry columns, then the extent "
                        + "cannot be computed");
            }
            int columnCount = 0;
            StringBuilder mainSelect = new StringBuilder("SELECT ");
            StringBuilder subSELECT = new StringBuilder("SELECT ");
            if (dbTypes== H2GIS|| dbTypes==H2) {
                for (int i = 0; i < geometryColumns.length; i++) {
                    String geomField = geometryColumns[i];
                    if (i > 0) {
                        mainSelect.append(",");
                        subSELECT.append(",");
                    }
                    if (geomField != null && !geomField.isEmpty()) {
                        String columnName = "geom_" + i;
                        subSELECT.append(geomField).append(" as ").append(columnName);
                        mainSelect.append("ST_EXTENT(").append(columnName).append(")").append(" as ").append(columnName);
                        columnCount++;
                    }
                }
            } else {
                for (int i = 0; i < geometryColumns.length; i++) {
                    String geomField = geometryColumns[i];
                    if (i > 0) {
                        mainSelect.append(",");
                        subSELECT.append(",");
                    }
                    if (geomField != null && !geomField.isEmpty()) {
                        String columnName = "geom_" + i;
                        subSELECT.append(geomField).append(" as ").append(columnName);
                        mainSelect.append(" ST_SetSRID(ST_EXTENT(").append(columnName).append("), MAX(ST_SRID(")
                                .append(columnName).
                                append(" ))) as ").append(columnName);
                        columnCount++;
                    }
                }
            }
            mainSelect.append(" FROM ");
            subSELECT.append(" FROM ").append(location.toString()).append(" ");
            if (filter != null && !filter.isEmpty()) {
                subSELECT.append(filter);
            }
            subSELECT.append(" ) as foo");
            mainSelect.append("(").append(subSELECT.toString());
            Envelope aggregatedEnvelope = new Envelope();
            int srid = 0;
            try (ResultSet rs = connection.createStatement().executeQuery(mainSelect.toString())) {
                if (rs.next()) {
                    for (int i = 0; i < columnCount; i++) {
                        Geometry geom = (Geometry) rs.getObject(i + 1);
                        if (geom != null) {
                            int currentSRID = geom.isEmpty()?0:geom.getSRID();
                            if (srid == 0) {
                                srid = currentSRID;
                            } else if (srid != currentSRID) {
                                throw new SQLException("Operation on mixed SRID geometries not supported");
                            }
                            aggregatedEnvelope.expandToInclude(geom.getEnvelopeInternal());
                        }
                    }
                }
            }
            if (aggregatedEnvelope.isNull()) {
                return null;
            } else {
                Geometry geom = new GeometryFactory().toGeometry(aggregatedEnvelope);
                geom.setSRID(srid);
                return geom;
            }
        }
        throw new SQLException("Database not supported");
    }
    
    /**
     *
     * Merge the bounding box of all geometries inside a provided subquery,
     * geometry columns
     *
     * Note that the geometry column can be an expression.
     *
     * Supported syntaxes
     * {@code
     * the_geom -> Column name 
     * st_buffer(the_geom, 20) -> Geometry function
     * }
     *
     *
     * @param connection Active connection (not closed by this function)
     * @param subQuery a subquery to filter the data
     * @param geometryColumns List of geometry columns or geometry functions
     *
     * @return Envelope of the table as Geometry
     *
     * @throws SQLException If the table not exists, empty or geometry field
     * empty.
     *
     */
    public static Geometry getEnvelope(Connection connection, String subQuery, String[] geometryColumns)
            throws SQLException {
        return getEnvelope(connection, subQuery, geometryColumns, null);
    }
    
    /**
     *
     * Merge the bounding box of all geometries inside a provided subquery,
     * geometry columns and filter condition
     *
     * Note that the geometry column can be an expression.
     *
     * Supported syntaxes
     * {@code
     * the_geom -> Column name 
     * st_buffer(the_geom, 20) -> Geometry function
     * }
     *
     *
     * @param connection Active connection (not closed by this function)
     * @param subQuery a subquery to filter the data
     * @param geometryColumns List of geometry columns or geometry functions
     * @param filter filter condition after the from
     *
     * @return Envelope of the table as Geometry
     *
     * @throws SQLException If the table not exists, empty or geometry field
     * empty.
     *
     */
    public static Geometry getEnvelope(Connection connection, String subQuery, String[] geometryColumns, String filter)
            throws SQLException {
        if (geometryColumns == null || geometryColumns.length == 0) {
            throw new SQLException("Geometry columns cannot be null or empty");
        }
        if(subQuery==null || subQuery.isEmpty()){
            throw new SQLException("The subquery cannot be null or empty");
        }
        DBTypes dbTypes = DBUtils.getDBType(connection);
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes==H2 || dbTypes==POSTGRESQL) {
            int columnCount = 0;
            StringBuilder sb = new StringBuilder("SELECT ");
            if (dbTypes==H2GIS || dbTypes==H2) {
                for (int i = 0; i < geometryColumns.length; i++) {
                    String geomField = geometryColumns[i];
                    if (i > 0) {
                        sb.append(",");
                    }
                    if (geomField != null && !geomField.isEmpty()) {
                        sb.append("ST_EXTENT(").append(geomField).append(")").append(" as geom_").append(i);
                        columnCount++;
                    }
                }
            } else {
                for (int i = 0; i < geometryColumns.length; i++) {
                    String geomField = geometryColumns[i];
                    if (i > 0) {
                        sb.append(",");
                    }
                    if (geomField != null && !geomField.isEmpty()) {
                        sb.append(" ST_SetSRID(ST_EXTENT(").append(geomField).append("), MAX(ST_SRID(")
                                .append(geomField).
                                append(" ))) as geom_").append(i);
                        columnCount++;
                    }
                }
            }
            sb.append(" FROM ").append("(").append(subQuery).append(") as foo");
            if (filter != null && !filter.isEmpty()) {
                sb.append(" ").append(filter);
            }
            Envelope aggregatedEnvelope = new Envelope();
            int srid = 0;
            try (ResultSet rs = connection.createStatement().executeQuery(sb.toString())) {
                if (rs.next()) {
                    for (int i = 0; i < columnCount; i++) {
                        Geometry geom = (Geometry) rs.getObject(i + 1);
                        if (geom != null) {
                            int currentSRID = geom.isEmpty()?0:geom.getSRID();
                            if (srid == 0) {
                                srid = currentSRID;
                            } else if (srid != currentSRID) {
                                throw new SQLException("Operation on mixed SRID geometries not supported");
                            }
                            aggregatedEnvelope.expandToInclude(geom.getEnvelopeInternal());
                        }
                    }
                }
            }
            if (aggregatedEnvelope.isNull()) {
                return null;
            } else {
                Geometry geom = new GeometryFactory().toGeometry(aggregatedEnvelope);
                geom.setSRID(srid);
                return geom;
            }
        }
        throw new SQLException("DataBase not supported");
    }

    /**
     * Merge the bounding box of all geometries inside the provided table.
     *
     * Use the first geometry column
     *
     * @param connection Active connection (not closed by this function)
     * @param table table name
     *
     * @return Envelope of the table as Geometry
     *
     * @throws SQLException If the table not exists, empty or does not contain a
     * geometry field.
     */
    public static Geometry getEnvelope(Connection connection, String table)
            throws SQLException {
        return getEnvelope(connection, TableLocation.parse(table, DBUtils.getDBType(connection)));
    }

    /**
     * Merge the bounding box of all geometries inside the provided table.
     *
     * Use the first geometry column
     *
     * @param connection Active connection (not closed by this function)
     * @param location Location of the table
     *
     * @return Envelope of the table as Geometry
     *
     * @throws SQLException If the table not exists, empty or does not contain a
     * geometry field.
     */
    public static Geometry getEnvelope(Connection connection, TableLocation location)
            throws SQLException {
        LinkedHashMap<String, Integer> geometryFields = GeometryTableUtilities.getGeometryColumnNamesAndIndexes(connection, location);
        if (geometryFields.isEmpty()) {
            throw new SQLException("The table " + location + " does not contain a Geometry field, then the extent "
                    + "cannot be computed");
        }
        return getEnvelope(connection, location, geometryFields.keySet().stream().findFirst().get());
    }
    
    
    /**
     * Return an array of two string if the input SRID exists in the spatial ref table.
     * The array contains the authority name and
     * its SRID code. 
     * If the SRID does not exist return null
     * 
     * @param connection Active connection
     * @param srid srid to check
     * 
     * @return Array of two string that correspond to the authority name and its
     * SRID code
     */
    public static String[] getAuthorityAndSRID(Connection connection, int srid)
            throws SQLException {
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
                    return new String[]{authority, sridCode};
                }
            } finally {
                ps.close();
            }
        }
        return null;        
    }
    
    
    /**
     * Return an array of two string that correspond to the authority name and
     * its SRID code.If the SRID does not exist return the array {null, null}
     *
     * @param connection Active connection
     * @param table Table name
     * @param geometryColumnName Field to analyse
     *
     * @return Array of two string that correspond to the authority name and its
     * SRID code
     *
     */
    public static String[] getAuthorityAndSRID(Connection connection, String table, String geometryColumnName) throws SQLException{
        return getAuthorityAndSRID(connection, TableLocation.parse(table, DBUtils.getDBType(connection)), geometryColumnName);
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
    
    /**
     * Change the SRID of the table
     *
     * @param connection Active connection
     * @param table Table name
     * @param geometryColumnName geometry column name
     * @param srid to force
     * @return true if query is well executed
     */
    public static boolean alterSRID(Connection connection, String table, String geometryColumnName, int srid) throws SQLException {
        return alterSRID(connection, TableLocation.parse(table, DBUtils.getDBType(connection)), geometryColumnName, srid);
    }

    /**
     * Change the SRID of the table
     *
     * @param connection Active connection
     * @param tableLocation Table name
     * @param geometryColumnName geometry column name
     * @param srid to force
     * @return true if query is well executed
     */
    public static boolean alterSRID(Connection connection, TableLocation tableLocation, String geometryColumnName, int srid) throws SQLException {
        DBTypes dbTypes = tableLocation.getDbTypes();
        if(dbTypes==H2GIS || dbTypes== POSTGIS || dbTypes==H2 || dbTypes==POSTGRESQL) {
            if (srid >= 0) {
                String tableName = tableLocation.toString();
                if (tableName.isEmpty()) {
                    throw new SQLException("The table name cannot be empty");
                }
                String fieldName = TableLocation.capsIdentifier(geometryColumnName, dbTypes);
                GeometryMetaData metadata = GeometryTableUtilities.getMetaData(connection, tableLocation, fieldName);
                if (metadata != null) {
                    if (metadata.getSRID() == srid) {
                        return false;
                    }
                    fieldName = TableLocation.quoteIdentifier(fieldName, dbTypes);
                    String geometrySignature = "GEOMETRY" + "(" + metadata.geometryType + "," + srid + ")";
                    String query = "ALTER TABLE " + tableName + " ALTER COLUMN " + fieldName +
                            " TYPE " + geometrySignature + " USING ST_SetSRID(" + fieldName + "," + srid + ")";
                    connection.createStatement().execute(query);
                    return true;
                } else {
                    return false;
                }
            }
            throw new SQLException("The SRID value must be greater or equal than 0");
        }
        throw new SQLException("DataBase not supported");
    }

    /**
     * Check if the geometry column has a spatial index
     *
     * @param connection Active connection
     * @param tableLocation Table name
     * @param geometryColumnName geometry column name
     * @return true if query is well executed
     */
    public static boolean isSpatialIndexed(Connection connection, TableLocation tableLocation, String geometryColumnName) throws SQLException {
        return JDBCUtilities.isSpatialIndexed(connection,tableLocation, geometryColumnName);
    }
}
