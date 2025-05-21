/*
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

import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.DataSourceWrapper;

import javax.sql.DataSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.h2gis.utilities.dbtypes.DBTypes.*;
import static org.h2gis.utilities.dbtypes.DBUtils.getDBType;

/**
 * DBMS should follow standard but it is not always the case, this class do some
 * common operations. Compatible with H2 and PostgreSQL.
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher
 * @author Adam Gouge
 * @author Sylvain PALOMINOS (UBS chaire GEOTERA 2020)
 */
public class JDBCUtilities {

    public static final String H2_DRIVER_PACKAGE_NAME = "org.h2.jdbc";
    public static final int POSTGRES_MAX_VARCHAR = 10485760;

    public enum TABLE_TYPE {
        TABLE, VIEW, FOREIGN_TABLE, TEMPORARY, TABLE_LINK, UNKOWN;

        /**
         * Build a new {@code TABLE_TYPE} from a {@code String table_type_name}.
         *
         * @param table_type_name table type
         * @return A {@code TABLE_TYPE} value.
         */
        public static TABLE_TYPE fromString(String table_type_name) {
            String token = table_type_name == null ? "" : table_type_name;
            if (token.contains("BASE TABLE")) {
                return TABLE;
            } else if (token.equals("TABLE")) {
                return TABLE;
            } else if (token.contains("SYSTEM TABLE")) {
                return TABLE;
            } else if (token.contains("VIEW")) {
                return VIEW;
            } else if (token.contains("FOREIGN TABLE")) {
                return FOREIGN_TABLE;
            } else if (token.contains("TEMPORARY")) {
                return TEMPORARY;
            } else if (token.contains("TABLE LINK")) {
                return TABLE_LINK;
            } else {
                return UNKOWN;
            }
        }
    }

    public enum FUNCTION_TYPE {
        ALL, BUILT_IN, ALIAS
    }

    public static final String H2_DRIVER_NAME = "H2 JDBC Driver";

    private JDBCUtilities() {
    }

    private static ResultSet getTablesView(Connection connection, String catalog, String schema, String table) throws SQLException {
        Integer catalogIndex = null;
        Integer schemaIndex = null;
        Integer tableIndex = 1;
        StringBuilder sb = new StringBuilder("SELECT * from INFORMATION_SCHEMA.TABLES where ");
        if (!catalog.isEmpty()) {
            sb.append("UPPER(table_catalog) = ? AND ");
            catalogIndex = 1;
            tableIndex++;
        }
        if (!schema.isEmpty()) {
            sb.append("UPPER(table_schema) = ? AND ");
            schemaIndex = tableIndex;
            tableIndex++;
        }
        sb.append("UPPER(table_name) = ? ");
        PreparedStatement geomStatement = connection.prepareStatement(sb.toString());
        if (catalogIndex != null) {
            geomStatement.setString(catalogIndex, catalog.toUpperCase());
        }
        if (schemaIndex != null) {
            geomStatement.setString(schemaIndex, schema.toUpperCase());
        }
        geomStatement.setString(tableIndex, table.toUpperCase());
        return geomStatement.executeQuery();
    }

    /**
     * Return true if the table contains the field.
     *
     * @param connection Connection
     * @param table      a TableLocation
     * @param fieldName  Field name
     * @return True if the table contains the field
     */
    public static boolean hasField(Connection connection, TableLocation table, String fieldName) throws SQLException {
        return hasField(connection, table.toString(), fieldName);
    }

    /**
     * Return true if table tableName contains field fieldName.
     *
     * @param connection Connection
     * @param tableName  a table name in the form CATALOG.SCHEMA.TABLE
     * @param fieldName  Field name
     * @return True if the table contains the field
     */
    public static boolean hasField(Connection connection, String tableName, String fieldName) throws SQLException {
        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableName + " LIMIT 0;");
            try {
                return hasField(resultSet.getMetaData(), fieldName);
            } finally {
                resultSet.close();
            }
        } catch (SQLException ex) {
            return false;
        } finally {
            statement.close();
        }
    }

    private static boolean hasField(ResultSetMetaData resultSetMetaData, String fieldName) throws SQLException {
        return getFieldIndex(resultSetMetaData, fieldName) != -1;
    }

    /**
     * Fetch the metadata, and check field name
     *
     * @param resultSetMetaData Active result set meta data.
     * @param fieldName         Field name, ignore case
     * @return The field index [1-n]; -1 if the field is not found
     */
    public static int getFieldIndex(ResultSetMetaData resultSetMetaData, String fieldName) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        for (int columnId = 1; columnId <= columnCount; columnId++) {
            if (fieldName.equalsIgnoreCase(resultSetMetaData.getColumnName(columnId))) {
                return columnId;
            }
        }
        return -1;
    }

    /**
     * Check column name from its index
     *
     * @param resultSetMetaData Active result set meta data.
     * @param columnIndex       Column index
     * @return The column name
     */
    public static String getColumnName(ResultSetMetaData resultSetMetaData, Integer columnIndex) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        for (int columnId = 1; columnId <= columnCount; columnId++) {
            if (columnId == columnIndex) {
                return resultSetMetaData.getColumnName(columnId);
            }
        }
        return null;
    }

    /**
     * @param connection  Active connection to the database
     * @param table       a TableLocation
     * @param columnIndex Field ordinal position [1-n]
     * @return The field name, empty if the field position or table is not found
     * @throws SQLException If jdbc throws an error
     */
    public static String getColumnName(Connection connection, TableLocation table, int columnIndex) throws SQLException {
        return getColumnName(connection, table.toString(), columnIndex);
    }

    /**
     * @param connection  Active connection to the database
     * @param tableName   a table name in the form CATALOG.SCHEMA.TABLE
     * @param columnIndex Field ordinal position [1-n]
     * @return The field name, empty if the field position or table is not found
     * @throws SQLException If jdbc throws an error
     */
    public static String getColumnName(Connection connection, String tableName, int columnIndex) throws SQLException {
        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableName + " LIMIT 0;");
            try {
                return getColumnName(resultSet.getMetaData(), columnIndex);
            } finally {
                resultSet.close();
            }
        } finally {
            statement.close();
        }
    }

    /**
     * Returns the list of all the column names of a table.
     *
     * @param connection Active connection to the database
     * @param table      a TableLocation
     * @return The list of field name.
     * @throws SQLException If jdbc throws an error
     */
    public static List<String> getColumnNames(Connection connection, TableLocation table) throws SQLException {
        return getColumnNames(connection, table.toString());
    }

    /**
     * Returns the list of all the column names of a table.
     *
     * @param connection Active connection to the database
     * @param tableName  a table name in the form CATALOG.SCHEMA.TABLE
     * @return The list of field name.
     * @throws SQLException If jdbc throws an error
     */
    public static List<String> getColumnNames(Connection connection, String tableName) throws SQLException {
        List<String> fieldNameList = new ArrayList<>();
        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableName + " LIMIT 0;");
            try {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int columnCount = metadata.getColumnCount();
                for (int columnId = 1; columnId <= columnCount; columnId++) {
                    fieldNameList.add(metadata.getColumnName(columnId));
                }
            } finally {
                resultSet.close();
            }
        } finally {
            statement.close();
        }
        return fieldNameList;
    }

    /**
     * Returns the list of all the column names and indexes of a table.
     *
     * @param connection Active connection to the database
     * @param table      a TableLocation
     * @return The list of field name.
     * @throws SQLException If jdbc throws an error
     */
    public static List<Tuple<String, Integer>> getColumnNamesAndIndexes(Connection connection, TableLocation table) throws SQLException {
        return getColumnNamesAndIndexes(connection, table.toString());
    }

    /**
     * Returns the list of all the column names and indexes of a table.
     *
     * @param connection Active connection to the database
     * @param tableName  a table name in the form CATALOG.SCHEMA.TABLE
     * @return The list of field name.
     * @throws SQLException If jdbc throws an error
     */
    public static List<Tuple<String, Integer>> getColumnNamesAndIndexes(Connection connection, String tableName) throws SQLException {
        List<Tuple<String, Integer>> fieldNameList = new ArrayList<>();
        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableName + " LIMIT 0;");
            try {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int columnCount = metadata.getColumnCount();
                for (int columnId = 1; columnId <= columnCount; columnId++) {
                    fieldNameList.add(new Tuple<>(metadata.getColumnName(columnId), columnId));
                }
            } finally {
                resultSet.close();
            }
        } finally {
            statement.close();
        }
        return fieldNameList;
    }

    /**
     * Fetch the row count of a table.
     *
     * @param connection Active connection.
     * @param table      a TableLocation
     * @return Row count
     * @throws SQLException If the table does not exists, or sql request fail.
     */
    public static int getRowCount(Connection connection, TableLocation table) throws SQLException {
        return getRowCount(connection, table.toString());
    }

    /**
     * Fetch the row count of a table.
     *
     * @param connection Active connection.
     * @param tableName  a table name in the form CATALOG.SCHEMA.TABLE
     * @return Row count
     * @throws SQLException If the table does not exist, or sql request fail.
     */
    public static int getRowCount(Connection connection, String tableName) throws SQLException {
        Statement st = connection.createStatement();
        int rowCount = 0;
        try {
            ResultSet rs = st.executeQuery(String.format("select count(*) rowcount from %s", tableName));
            try {
                if (rs.next()) {
                    rowCount = rs.getInt(1);
                }
            } finally {
                rs.close();
            }
        } finally {
            st.close();
        }
        return rowCount;
    }

    /**
     * Read INFORMATION_SCHEMA.TABLES in order to see if the provided table
     * reference is a temporary table.
     *
     * @param connection    Active connection not closed by this method
     * @param tableLocation Table reference
     * @return True if the provided table is temporary.
     * @throws SQLException If the table does not exist.
     */
    public static boolean isTemporaryTable(Connection connection, TableLocation tableLocation) throws SQLException {
        ResultSet rs = getTablesView(connection, tableLocation.getCatalog(), tableLocation.getSchema(), tableLocation.getTable());
        boolean isTemporary = false;
        try {
            if (rs.next()) {
                String tableType;
                if (hasField(rs.getMetaData(), "STORAGE_TYPE")) {
                    // H2
                    tableType = rs.getString("STORAGE_TYPE");
                } else {
                    // Standard SQL
                    tableType = rs.getString("TABLE_TYPE");
                }
                isTemporary = tableType.contains("TEMPORARY");
            } else {
                throw new SQLException("The table " + tableLocation.toString() + " does not exists");
            }
        } finally {
            rs.close();
        }
        return isTemporary;
    }

    /**
     * Read INFORMATION_SCHEMA.TABLES in order to see if the provided table
     * reference is a linked table.
     *
     * @param connection Active connection not closed by this method
     * @param table      TableLocation
     * @return True if the provided table is linked.
     * @throws SQLException If the table does not exist.
     */
    public static boolean isLinkedTable(Connection connection, TableLocation table) throws SQLException {
        return isLinkedTable(connection, table.toString());
    }

    /**
     * Read INFORMATION_SCHEMA.TABLES in order to see if the provided table
     * reference is a linked table.
     *
     * @param connection     Active connection not closed by this method
     * @param tableReference Table reference
     * @return True if the provided table is linked.
     * @throws SQLException If the table does not exist.
     */
    public static boolean isLinkedTable(Connection connection, String tableReference) throws SQLException {
        String[] location = TableLocation.split(tableReference);
        ResultSet rs = getTablesView(connection, location[0], location[1], location[2]);
        boolean isLinked;
        try {
            if (rs.next()) {
                String tableType = rs.getString("STORAGE_TYPE");
                isLinked = tableType.contains("TABLE LINK");
            } else {
                throw new SQLException("The table " + tableReference + " does not exists");
            }
        } finally {
            rs.close();
        }
        return isLinked;
    }

    /**
     * @param connection to the database
     * @return True if the provided metadata is a h2 database connection.
     */
    public static boolean isH2DataBase(Connection connection) throws SQLException {
        if (connection.getClass().getName().startsWith(H2_DRIVER_PACKAGE_NAME)
                || connection.getClass().equals(ConnectionWrapper.class)) {
            return true;
        } else {
            return connection.getMetaData().getDriverName().equals("H2 JDBC Driver");
        }
    }

    /**
     * @param connection    Connection
     * @param tableLocation table identifier
     * @return The integer primary key used for edition[1-n]; 0 if the source is
     * closed or if the table has no primary key or more than one column as
     * primary key
     */
    public static int getIntegerPrimaryKey(Connection connection, TableLocation tableLocation) throws SQLException {
        if (!tableExists(connection, tableLocation)) {
            throw new SQLException("Table " + tableLocation + " not found.");
        }
        final DatabaseMetaData meta = connection.getMetaData();
        String columnNamePK = null;
        ResultSet rs = meta.getPrimaryKeys(tableLocation.getCatalog(null), tableLocation.getSchema(null),
                tableLocation.getTable());
        try {
            while (rs.next()) {
                // If the schema is not specified, public must be the schema
                if (!tableLocation.getSchema().isEmpty() || "public".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
                    if (columnNamePK == null) {
                        columnNamePK = rs.getString("COLUMN_NAME");
                    } else {
                        // Multi-column PK is not supported
                        columnNamePK = null;
                        break;
                    }
                }
            }
        } finally {
            rs.close();
        }
        if (columnNamePK != null) {
            rs = meta.getColumns(tableLocation.getCatalog(null), tableLocation.getSchema(null),
                    tableLocation.getTable(), columnNamePK);
            try {
                while (rs.next()) {
                    if (!tableLocation.getSchema().isEmpty() || "public".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
                        int dataType = rs.getInt("DATA_TYPE");
                        if (dataType == Types.BIGINT || dataType == Types.INTEGER || dataType == Types.ROWID) {
                            return rs.getInt("ORDINAL_POSITION");
                        }
                    }
                }
            } finally {
                rs.close();
            }
        }
        return 0;
    }

    /**
     * Method to fetch an integer primary key (name + index). Return null
     * otherwise
     *
     * @param connection    Connection
     * @param tableLocation table identifier
     * @return The name and the index of an integer primary key used for
     * edition[1-n]; 0 if the source is closed or if the table has no primary
     * key or more than one column as primary key
     */
    public static Tuple<String, Integer> getIntegerPrimaryKeyNameAndIndex(Connection connection, TableLocation tableLocation) throws SQLException {
        if (!tableExists(connection, tableLocation)) {
            throw new SQLException("Table " + tableLocation + " not found.");
        }
        final DatabaseMetaData meta = connection.getMetaData();
        String columnNamePK = null;
        ResultSet rs = meta.getPrimaryKeys(tableLocation.getCatalog(null), tableLocation.getSchema(null),
                tableLocation.getTable());
        try {
            while (rs.next()) {
                // If the schema is not specified, public must be the schema
                if (!tableLocation.getSchema().isEmpty() || "public".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
                    if (columnNamePK == null) {
                        columnNamePK = rs.getString("COLUMN_NAME");
                    } else {
                        // Multi-column PK is not supported
                        columnNamePK = null;
                        break;
                    }
                }
            }
        } finally {
            rs.close();
        }
        if (columnNamePK != null) {
            rs = meta.getColumns(tableLocation.getCatalog(null), tableLocation.getSchema(null),
                    tableLocation.getTable(), columnNamePK);
            try {
                while (rs.next()) {
                    if (!tableLocation.getSchema().isEmpty() || "public".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
                        int dataType = rs.getInt("DATA_TYPE");
                        if (dataType == Types.BIGINT || dataType == Types.INTEGER || dataType == Types.ROWID) {
                            return new Tuple<>(columnNamePK, rs.getInt("ORDINAL_POSITION"));
                        }
                    }
                }
            } finally {
                rs.close();
            }
        }
        return null;
    }

    /**
     * Return true if the table exists.
     *
     * @param connection    Connection to the database
     * @param tableLocation Table name
     * @return true if the table exists
     */
    public static boolean tableExists(Connection connection, TableLocation tableLocation) throws SQLException {
        List<String> tableNames = JDBCUtilities.getTableNames(connection,
                tableLocation.getCatalog().isEmpty() ? null : tableLocation.getCatalog(),
                tableLocation.getSchema().isEmpty() ? null : tableLocation.getSchema(),
                null,
                new String[]{"TABLE", "VIEW", "SYSTEM TABLE"});
        for (String matchTableName : tableNames) {
            TableLocation matchTableNameLocation = TableLocation.parse(matchTableName, tableLocation.getDbTypes());
            if (tableLocation.getTable().equals(matchTableNameLocation.getTable()) &&
                    ((tableLocation.getSchema().isEmpty() &&
                            matchTableNameLocation.getSchema().equalsIgnoreCase("public")) ||
                            tableLocation.getSchema().equals(matchTableNameLocation.getSchema()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the table exists.
     *
     * @param connection Connection to the database
     * @param tableName  Table name
     * @return true if the table exists
     */
    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DBTypes dbTypes = DBUtils.getDBType(connection);
        return tableExists(connection, TableLocation.parse(tableName, dbTypes));
    }

    /**
     * Returns the list of table names.
     *
     * @param connection    Active connection to the database
     * @param tableLocation Table name
     * @param types         A list of table types, which must be from the list of table
     *                      types returned from getTableTypes(), to include. null returns all types
     * @return The integer primary key used for edition[1-n]; 0 if the source is
     * closed or if the table has no primary key or more than one column as
     * primary key
     */
    public static List<String> getTableNames(Connection connection, TableLocation tableLocation, String[] types) throws SQLException {
        List<String> tableList = new ArrayList<>();
        ResultSet rs = connection.getMetaData().getTables(tableLocation.getCatalog(),
                tableLocation.getSchema("PUBLIC"),
                tableLocation.getTable().replace(TableLocation.QUOTE_CHAR, ""), types);
        try {
            while (rs.next()) {
                tableList.add(new TableLocation(rs).toString(tableLocation.getDbTypes()));
            }
        } finally {
            rs.close();
        }
        return tableList;
    }

    /**
     * Returns the list of table names.
     *
     * @param connection       Active connection to the database
     * @param catalog          A catalog name. Must match the catalog name as it is
     *                         stored in the database. "" retrieves those without a catalog; null means
     *                         that the catalog name should not be used to narrow the search
     * @param schemaPattern    A schema name pattern. Must match the schema name as
     *                         it is stored in the database. "" retrieves those without a schema. null
     *                         means that the schema name should not be used to narrow the search
     * @param tableNamePattern A table name pattern. Must match the table name
     *                         as it is stored in the database
     * @param types            A list of table types, which must be from the list of table
     *                         types returned from getTableTypes(), to include. null returns all types
     * @return The integer primary key used for edition[1-n]; 0 if the source is
     * closed or if the table has no primary key or more than one column as
     * primary key
     */
    public static List<String> getTableNames(Connection connection, String catalog, String schemaPattern,
                                             String tableNamePattern, String[] types) throws SQLException {
        List<String> tableList = new ArrayList<>();
        ResultSet rs = connection.getMetaData().getTables(catalog, schemaPattern, tableNamePattern, types);
        final DBTypes dbType = getDBType(connection);
        try {
            while (rs.next()) {
                tableList.add(new TableLocation(rs).toString(dbType));
            }
        } finally {
            rs.close();
        }
        return tableList;
    }

    /**
     * Returns the list of distinct values contained by a field from a table
     * from the database
     *
     * @param connection Connection
     * @param table      Name of the table containing the field.
     * @param fieldName  Name of the field containing the values.
     * @return The list of distinct values of the field.
     */
    public static List<String> getUniqueFieldValues(Connection connection, TableLocation table, String fieldName) throws SQLException {
        return getUniqueFieldValues(connection, table.toString(), fieldName);
    }

    /**
     * Returns the list of distinct values contained by a field from a table
     * from the database
     *
     * @param connection Connection
     * @param tableName  Name of the table containing the field.
     * @param fieldName  Name of the field containing the values.
     * @return The list of distinct values of the field.
     */
    public static List<String> getUniqueFieldValues(Connection connection, String tableName, String fieldName) throws SQLException {
        final DBTypes dbType = getDBType(connection);
        final Statement statement = connection.createStatement();
        List<String> fieldValues = new ArrayList<>();
        try {
            ResultSet result = statement.executeQuery("SELECT DISTINCT " + TableLocation.quoteIdentifier(fieldName) + " FROM " + TableLocation.parse(tableName).toString(dbType));
            try {
                while (result.next()) {
                    fieldValues.add(result.getString(1));
                }
            } finally {
                result.close();
            }
        } finally {
            statement.close();
        }
        return fieldValues;
    }

    /**
     * A method to create an empty table (no columns)
     *
     * @param connection Connection
     * @param table      Table name
     */
    public static void createEmptyTable(Connection connection, TableLocation table) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + table.toString() + " ()");
        }
    }

    /**
     * A method to create an empty table (no columns)
     *
     * @param connection     Connection
     * @param tableReference Table name
     */
    public static void createEmptyTable(Connection connection, String tableReference) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tableReference + " ()");
        }
    }

    /**
     * Fetch the name of columns
     *
     * @param resultSetMetaData Active result set meta data.
     * @return An array with all column names
     */
    public static List<String> getColumnNames(ResultSetMetaData resultSetMetaData) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        int cols = resultSetMetaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            columnNames.add(resultSetMetaData.getColumnName(i));
        }
        return columnNames;
    }

    /**
     * In order to be able to use {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get
     * {@link SpatialResultSet} and {@link SpatialResultSetMetaData} this method
     * wrap the provided dataSource.
     *
     * @param dataSource H2 or PostGIS DataSource
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
     * @param st              Statement to cancel
     * @param progressVisitor Progress to link with
     * @return call
     * {@link org.h2gis.api.ProgressVisitor#removePropertyChangeListener(java.beans.PropertyChangeListener)}
     * with this object as argument
     */
    public static PropertyChangeListener attachCancelResultSet(Statement st, ProgressVisitor progressVisitor) {
        PropertyChangeListener propertyChangeListener = new CancelResultSet(st);
        progressVisitor.addPropertyChangeListener(ProgressVisitor.PROPERTY_CANCELED, propertyChangeListener);
        return propertyChangeListener;
    }

    /**
     * Call cancel of statement
     */
    private static final class CancelResultSet implements PropertyChangeListener {

        private final Statement st;

        private CancelResultSet(Statement st) {
            this.st = st;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                st.cancel();
            } catch (SQLException ex) {
                // Ignore
            }
        }
    }

    /**
     * Return the type of the table using an Enum
     *
     * @param connection to the database
     * @param location table name
     * @return the table type
     */
    public static TABLE_TYPE getTableType(Connection connection, TableLocation location) throws SQLException {
        DBTypes dbType = location.getDbTypes();
        try (ResultSet rs = getTablesView(connection, location.getCatalog(), location.getSchema(), location.getTable())) {
            if (rs.next()) {
                if (dbType == H2) {
                    // H2                   
                    String storage = rs.getString("STORAGE_TYPE");
                    if (storage.contains("TEMPORARY")) {
                        return TABLE_TYPE.TEMPORARY;
                    } else if (storage.equals("TABLE LINK")) {
                        return TABLE_TYPE.TABLE_LINK;
                    } else {
                        return TABLE_TYPE.fromString(rs.getString("TABLE_TYPE"));
                    }
                } else {
                    // Standard SQL
                    return TABLE_TYPE.fromString(rs.getString("TABLE_TYPE"));
                }
            } else {
                throw new SQLException("The table " + location + " does not exists");
            }
        }
    }

    /**
     * A simple method to generate a DDL create table command from a table name
     * Takes into account only data types
     *
     * @param connection database
     * @param sourceTable source table name
     * @param targetTable target table name
     * @return a create table ddl command
     */
    public static String createTableDDL(Connection connection, TableLocation sourceTable, TableLocation targetTable) throws SQLException {
        if (sourceTable == null) {
            throw new SQLException("The source table name cannot be null or empty");
        }
        if (targetTable == null) {
            throw new SQLException("The target table name cannot be null or empty");
        }
        if (JDBCUtilities.tableExists(connection, sourceTable)) {
            final StringBuilder builder = new StringBuilder(256);
            LinkedHashMap<String, GeometryMetaData> geomMetadatas = GeometryTableUtilities.getMetaData(connection, sourceTable);
            builder.append("CREATE TABLE ").append(targetTable);
            final Statement statement = connection.createStatement();
            try {
                final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + sourceTable.toString() + " LIMIT 0;");
                try {
                    ResultSetMetaData metadata = resultSet.getMetaData();
                    int columnCount = metadata.getColumnCount();
                    if (columnCount > 0) {
                        builder.append(" (");
                    }
                    for (int i = 1; i <= columnCount; i++) {
                        if (i > 1) {
                            builder.append(",");
                        }
                        String columnName = metadata.getColumnName(i);
                        String columnTypeName = metadata.getColumnTypeName(i);
                        int columnType = metadata.getColumnType(i);
                        if (columnType == Types.VARCHAR || columnType == Types.LONGVARCHAR || columnType == Types.NVARCHAR || columnType == Types.LONGNVARCHAR) {
                            int precision = metadata.getPrecision(i);
                            //POSTGRESQL VARCHAR MAX SIZE
                            if (precision > POSTGRES_MAX_VARCHAR) {
                                builder.append(columnName).append(" ").append(columnTypeName);
                            } else {
                                builder.append(columnName).append(" ").append(columnTypeName);
                                builder.append("(").append(precision).append(")");
                            }
                        } else {
                            if (columnType == Types.CHAR) {
                                builder.append(columnName).append(" ").append(columnTypeName);
                                builder.append("(").append(metadata.getColumnDisplaySize(i)).append(")");
                            } else if (columnType == Types.DOUBLE) {
                                builder.append(columnName).append(" ").append("DOUBLE PRECISION");
                            } else if (columnTypeName.toLowerCase().startsWith("geometry")) {
                                if (geomMetadatas.isEmpty()) {
                                    builder.append(columnName).append(" ").append(columnTypeName);
                                } else {
                                    GeometryMetaData geomMetadata = geomMetadatas.get(columnName);
                                    if (geomMetadata.getGeometryTypeCode() == GeometryTypeCodes.GEOMETRY && geomMetadata.getSRID() == 0) {
                                        builder.append(columnName).append(" ").append(columnTypeName);
                                    } else {
                                        builder.append(columnName).append(" ").append("GEOMETRY")
                                                .append("(").append(geomMetadata.getGeometryType()).append(",").append(geomMetadata.getSRID()).append(")");
                                    }
                                }
                            } else if (columnTypeName.equalsIgnoreCase("decfloat")) {
                                builder.append(columnName).append(" FLOAT");
                            } else {
                                builder.append(columnName).append(" ").append(columnTypeName);
                            }
                        }
                    }
                    if (columnCount > 0) {
                        builder.append(")");
                    }
                    return builder.toString();
                } finally {
                    resultSet.close();
                }
            } finally {
                statement.close();
            }

        } else {
            throw new SQLException("The table " + sourceTable + " doesn't exist");
        }
    }

    /**
     * A simple method to generate a DDL create table command from a table name
     * <p>
     * Takes into account only data types
     *
     * @param connection database
     * @param location table name
     * @return a create table ddl command
     */
    public static String createTableDDL(Connection connection, TableLocation location) throws SQLException {
        return createTableDDL(connection, location, location);
    }

    /**
     * A simple method to generate a DDL create table command from a query
     * <p>
     * Takes into account only data types
     *
     * @param outputTableName table name
     * @param resultSet input resultset
     * @return a create table ddl command
     */
    public static String createTableDDL(ResultSet resultSet, String outputTableName) throws SQLException {
        return createTableDDL(resultSet.getMetaData(), outputTableName);
    }

    /**
     * A simple method to generate a DDL create table command from
     * ResultSetMetaData
     * <p>
     * Takes into account only data types
     *
     * @param outputTableName table name
     * @param metadata table metadata
     * @return a create table ddl command
     */
    public static String createTableDDL(ResultSetMetaData metadata, String outputTableName) throws SQLException {
        if (outputTableName == null || outputTableName.isEmpty()) {
            throw new SQLException("The target table name cannot be null or empty");
        }
        final StringBuilder builder = new StringBuilder(256);
        builder.append("CREATE TABLE ").append(outputTableName);
        int columnCount = metadata.getColumnCount();
        if (columnCount > 0) {
            builder.append(" (");
        }
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1) {
                builder.append(",");
            }
            String columnName = metadata.getColumnName(i);
            String columnTypeName = metadata.getColumnTypeName(i);
            int columnType = metadata.getColumnType(i);
            if (columnType == Types.VARCHAR || columnType == Types.LONGVARCHAR || columnType == Types.NVARCHAR || columnType == Types.LONGNVARCHAR) {
                int precision = metadata.getPrecision(i);
                //POSTGRESQL VARCHAR MAX SIZE
                if (precision > POSTGRES_MAX_VARCHAR) {
                    builder.append(columnName).append(" ").append(columnTypeName);
                } else {
                    builder.append(columnName).append(" ").append(columnTypeName);
                    builder.append("(").append(precision).append(")");
                }

            } else {
                if (columnType == Types.CHAR) {
                    builder.append(columnName).append(" ").append(columnTypeName);
                    builder.append("(").append(metadata.getColumnDisplaySize(i)).append(")");
                } else if (columnType == Types.DOUBLE) {
                    builder.append(columnName).append(" ").append("DOUBLE PRECISION");
                } else if (columnTypeName.toLowerCase().startsWith("geometry")) {
                    builder.append(columnName).append(" ").append(columnTypeName);
                } else if (columnTypeName.equalsIgnoreCase("decfloat")) {
                    builder.append(columnName).append(" FLOAT");
                } else {
                    builder.append(columnName).append(" ").append(columnTypeName);
                }
            }
        }
        if (columnCount > 0) {
            builder.append(")");
        }
        return builder.toString();
    }

    /**
     * Returns true if the given column name from the given table is indexed,
     * return false otherwise.
     *
     * @param connection {@link Connection} containing the table to check.
     * @param tableName  Name of the table to check.
     * @param columnName Name of the column to check.
     * @return True if the given column is indexed, false otherwise.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static boolean isIndexed(Connection connection, String tableName, String columnName) throws SQLException {
        return isIndexed(connection, TableLocation.parse(tableName, getDBType(connection)), columnName);
    }

    /**
     * Returns true if the given column name from the given table has an
     * indexed, return false otherwise.
     *
     * @param connection {@link Connection} containing the table to check.
     * @param table      {@link TableLocation} of the table to check.
     * @param columnName Name of the column to check.
     * @return True if the given column is indexed, false otherwise.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static boolean isIndexed(Connection connection, TableLocation table, String columnName) throws SQLException {
        if (connection == null || table == null) {
            throw new SQLException("Unable to get the index names");
        }
        DBTypes dbTypes = table.getDbTypes();
        columnName = TableLocation.capsIdentifier(columnName, dbTypes);
        DatabaseMetaData md = connection.getMetaData();
        ResultSet indexInfo = md.getIndexInfo(connection.getCatalog(), table.getSchema(), table.getTable(), false, true);
        while (indexInfo.next()) {
            if (columnName.equals(indexInfo.getString("COLUMN_NAME"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given column name from the given table is indexed,
     * return false otherwise.
     *
     * @param connection {@link Connection} containing the table to check.
     * @param tableName  Name of the table to check.
     * @param columnName Name of the column to check.
     * @return True if the given column is indexed, false otherwise.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static boolean isSpatialIndexed(Connection connection, String tableName, String columnName) throws SQLException {
        return isSpatialIndexed(connection, TableLocation.parse(tableName, getDBType(connection)), columnName);
    }

    /**
     * Returns true if the given column name from the given table is indexed,
     * return false otherwise.
     *
     * @param connection {@link Connection} containing the table to check.
     * @param table      {@link TableLocation} of the table to check.
     * @param columnName Name of the column to check.
     * @return True if the given column is indexed, false otherwise.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static boolean isSpatialIndexed(Connection connection, TableLocation table, String columnName) throws SQLException {
        if (connection == null || columnName == null || table == null) {
            throw new SQLException("Unable to find an index");
        }
        DBTypes dbType = table.getDbTypes();
        if (dbType == H2 || dbType == H2GIS) {
            PreparedStatement ps = connection.prepareStatement("SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEX_COLUMNS "
                    + "WHERE INFORMATION_SCHEMA.INDEX_COLUMNS.TABLE_NAME=? "
                    + "AND INFORMATION_SCHEMA.INDEX_COLUMNS.TABLE_SCHEMA=? "
                    + "AND INFORMATION_SCHEMA.INDEX_COLUMNS.COLUMN_NAME=?"
                    + "AND INFORMATION_SCHEMA.INDEX_COLUMNS.INDEX_NAME "
                    + "IN (SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES  WHERE "
                    + "INFORMATION_SCHEMA.INDEXES.TABLE_SCHEMA=? "
                    + "AND  INFORMATION_SCHEMA.INDEXES.TABLE_NAME= ?  "
                    + " AND INFORMATION_SCHEMA.INDEXES.INDEX_TYPE_NAME='SPATIAL INDEX')");
            String tableName = table.getTable();
            String schemaName = table.getSchema("PUBLIC");
            ps.setObject(1, tableName);
            ps.setObject(2, schemaName);
            ps.setObject(3, TableLocation.capsIdentifier(columnName, dbType));
            ps.setObject(4, schemaName);
            ps.setObject(5, tableName);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } else if (dbType == POSTGIS || dbType == POSTGRESQL) {
            String query = "SELECT  cls.relname, am.amname "
                    + "FROM  pg_class cls "
                    + "JOIN pg_am am ON am.oid=cls.relam where cls.oid "
                    + " in(select attrelid as pg_class_oid from pg_catalog.pg_attribute "
                    + " where attname = ? and attrelid in "
                    + "(select b.oid from pg_catalog.pg_indexes a, pg_catalog.pg_class b  where a.schemaname =? and a.tablename =? "
                    + "and a.indexname = b.relname)) and am.amname = 'gist' ;";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setObject(1, columnName);
            ps.setObject(2, table.getSchema("public"));
            ps.setObject(3, table.getTable());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } else {
            throw new SQLException("Database not supported");
        }
    }

    /**
     * Create an index on the given column of the given table on the given
     * connection.
     *
     * @param connection Connection to access to the desired table.
     * @param table      Table containing the column to index.
     * @param columnName Name of the column to index.
     * @return True if the column have been indexed, false otherwise.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static boolean createIndex(Connection connection, TableLocation table, String columnName) throws SQLException {
        if (connection == null || table == null || columnName == null) {
            throw new SQLException("Unable to create an index");
        }
        final DBTypes dbType = table.getDbTypes();
        final String tableName = table.toString();
        connection.createStatement().execute("CREATE INDEX IF NOT EXISTS " + tableName + "_" + columnName
                + " ON " + tableName + " (" + TableLocation.capsIdentifier(columnName, dbType) + ")");
        return true;
    }

    /**
     * Create an index on the given column of the given table on the given
     * connection.
     *
     * @param connection Connection to access to the desired table.
     * @param table      Name of the table containing the column to index.
     * @param columnName Name of the column to index.
     * @return True if the column have been indexed, false otherwise.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static boolean createIndex(Connection connection, String table, String columnName) throws SQLException {
        return createIndex(connection, TableLocation.parse(table, getDBType(connection)), columnName);
    }

    /**
     * Create a spatial index on the given column of the given table on the
     * given connection.
     *
     * @param connection Connection to access to the desired table.
     * @param table      Table containing the column to index.
     * @param columnName Name of the column to index.
     * @return True if the column have been indexed, false otherwise.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static boolean createSpatialIndex(Connection connection, TableLocation table, String columnName) throws SQLException {
        if (connection == null || table == null || columnName == null) {
            throw new SQLException("Unable to create a spatial index");
        }
        DBTypes dbTypes = table.getDbTypes();
        if (dbTypes == H2GIS || dbTypes == POSTGIS || dbTypes == H2 || dbTypes == POSTGRESQL) {
            if (dbTypes == H2 || dbTypes == H2GIS) {
                connection.createStatement().execute("CREATE SPATIAL INDEX IF NOT EXISTS " + table.toString() + "_" + columnName
                        + " ON " + table.toString() + " (" + TableLocation.capsIdentifier(columnName, dbTypes) + ")");
            } else {
                connection.createStatement().execute("CREATE INDEX IF NOT EXISTS " + table.toString() + "_" + columnName
                        + " ON " + table.toString() + " USING GIST (" + TableLocation.capsIdentifier(columnName, dbTypes) + ")");
            }
            return true;
        }
        throw new SQLException("DataBase not supported");
    }

    /**
     * Create a spatial index on the given column of the given table on the
     * given connection.
     *
     * @param connection Connection to access to the desired table.
     * @param table      Name of the table containing the column to index.
     * @param columnName Name of the column to index.
     * @return True if the column have been indexed, false otherwise.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static boolean createSpatialIndex(Connection connection, String table, String columnName) throws SQLException {
        return createSpatialIndex(connection, TableLocation.parse(table, getDBType(connection)), columnName);
    }

    /**
     * Drop the index of the given column of the given table on yhe given
     * connection.
     *
     * @param connection Connection to access to the desired table.
     * @param table      Table containing the column to drop index.
     * @param columnName Name of the column to drop index.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static void dropIndex(Connection connection, TableLocation table, String columnName) throws SQLException {
        List<String> indexes = getIndexNames(connection, table, columnName);
        String query = indexes.stream()
                .map(key -> "DROP INDEX " + key)
                .collect(Collectors.joining(";"));
        connection.createStatement().execute(query);
    }

    /**
     * Return the name of all indexes for a given table
     *
     * @param connection Connection to access to the desired table.
     * @param table      Table containing the column to get the index names.
     * @return a map with the index name and its column name
     */
    public static Map<String, String> getIndexNames(Connection connection, String table) throws SQLException {
        return getIndexNames(connection, TableLocation.parse(table, getDBType(connection)));
    }

    /**
     * Return the name of all indexes for a given table
     *
     * @param connection Connection to access to the desired table.
     * @param table      Table containing the column to get the index names.
     * @return a map with the index name and its column name
     */
    public static Map<String, String> getIndexNames(Connection connection, TableLocation table) throws SQLException {
        if (connection == null || table == null) {
            throw new SQLException("Unable to get the index names");
        }
        DatabaseMetaData md = connection.getMetaData();
        Map<String, String> indexes = new HashMap<>();
        ResultSet indexInfo = md.getIndexInfo(connection.getCatalog(), table.getSchema(), table.getTable(), false, true);
        while (indexInfo.next()) {
            String indexName = indexInfo.getString("INDEX_NAME");
            String columnName = indexInfo.getString("COLUMN_NAME");
            indexes.put(indexName, columnName);
        }
        return indexes;
    }

    /**
     * Return the name of indexes for a given table and column
     *
     * @param connection Connection to access to the desired table.
     * @param table      Table containing the column to get the index names.
     * @param columnName Name of the column.
     * @return a list of the index names
     */
    public static List<String> getIndexNames(Connection connection, String table, String columnName) throws SQLException {
        return getIndexNames(connection, TableLocation.parse(table, getDBType(connection)), columnName);
    }

    /**
     * Return the name of indexes for a given table and column
     *
     * @param connection Connection to access to the desired table.
     * @param table      Table containing the column to get the index names.
     * @param columnName Name of the column.
     * @return a list of the index names
     */
    public static List<String> getIndexNames(Connection connection, TableLocation table, String columnName) throws SQLException {
        if (connection == null || table == null) {
            throw new SQLException("Unable to get the index names");
        }
        DBTypes dbTypes = table.getDbTypes();
        columnName = TableLocation.capsIdentifier(columnName, dbTypes);
        DatabaseMetaData md = connection.getMetaData();
        ArrayList<String> indexes = new ArrayList<>();
        ResultSet indexInfo = md.getIndexInfo(connection.getCatalog(), table.getSchema(), table.getTable(), false, true);
        while (indexInfo.next()) {
            if (columnName.equals(indexInfo.getString("COLUMN_NAME"))) {
                indexes.add(indexInfo.getString("INDEX_NAME"));
            }
        }
        return indexes;
    }

    /**
     * Drop the index of the given column of the given table on yhe given
     * connection.
     *
     * @param connection Connection to access to the desired table.
     * @param table      Name of the table containing the column to drop index.
     * @param columnName Name of the column to drop index.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static void dropIndex(Connection connection, String table, String columnName) throws SQLException {
        dropIndex(connection, TableLocation.parse(table, getDBType(connection)), columnName);
    }

    /**
     * Drop the all the indexes of the given table on the given connection.
     *
     * @param connection Connection to access to the desired table.
     * @param table      Table containing the column to drop index.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static void dropIndex(Connection connection, TableLocation table) throws SQLException {
        if (connection == null || table == null) {
            throw new SQLException("Unable to drop index");
        }
        Map<String, String> indexes = getIndexNames(connection, table);
        String query = indexes.keySet().stream()
                .map(key -> "DROP INDEX " + key)
                .collect(Collectors.joining(";"));
        connection.createStatement().execute(query);
    }

    /**
     * Drop the all the indexes of the given table on yhe given connection.
     *
     * @param connection Connection to access to the desired table.
     * @param table      Name of the table containing the column to drop index.
     * @throws SQLException Exception thrown on SQL execution error.
     */
    public static void dropIndex(Connection connection, String table) throws SQLException {
        dropIndex(connection, TableLocation.parse(table, getDBType(connection)));
    }

    /**
     * Return a list of numeric column names
     *
     * @param resultSetMetaData the metadata of the table
     * @return a list of the numeric column names
     */
    public static List<String> getNumericColumns(ResultSetMetaData resultSetMetaData) throws SQLException {
        List<String> fieldNameList = new ArrayList<>();
        int columnCount = resultSetMetaData.getColumnCount();
        for (int columnId = 1; columnId <= columnCount; columnId++) {
            if (isNumeric(resultSetMetaData.getColumnType(columnId))) {
                fieldNameList.add(resultSetMetaData.getColumnName(columnId));
            }
        }
        return fieldNameList;
    }

    /**
     * Returns the list of all the numeric column names of a table.
     *
     * @param connection Active connection to the database
     * @param table      a TableLocation
     * @return The list of field name.
     * @throws SQLException If jdbc throws an error
     */
    public static List<String> getNumericColumns(Connection connection, TableLocation table) throws SQLException {
        return getNumericColumns(connection, table.toString());
    }

    /**
     * Returns the list of all the numeric column names of a table.
     *
     * @param connection Active connection to the database
     * @param tableName  a table name in the form CATALOG.SCHEMA.TABLE
     * @return The list of field names.
     * @throws SQLException If jdbc throws an error
     */
    public static List<String> getNumericColumns(Connection connection, String tableName) throws SQLException {
        List<String> fieldNameList = new ArrayList<>();
        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableName + " LIMIT 0;");
            try {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int columnCount = metadata.getColumnCount();
                for (int columnId = 1; columnId <= columnCount; columnId++) {
                    if (isNumeric(metadata.getColumnType(columnId))) {
                        fieldNameList.add(metadata.getColumnName(columnId));
                    }
                }
            } finally {
                resultSet.close();
            }
        } finally {
            statement.close();
        }
        return fieldNameList;
    }

    /**
     * Return the first numeric column otherwise null
     *
     * @param resultSetMetaData the metadata of the table
     * @return the name of the column
     */
    public static String getFirstNumericColumn(ResultSetMetaData resultSetMetaData) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        for (int columnId = 1; columnId <= columnCount; columnId++) {
            if (isNumeric(resultSetMetaData.getColumnType(columnId))) {
                return resultSetMetaData.getColumnName(columnId);
            }
        }
        return null;
    }

    /**
     * Returns the first numeric column name of a table.
     *
     * @param connection Active connection to the database
     * @param table      a TableLocation
     * @return The first numeric column name .
     * @throws SQLException If jdbc throws an error
     */
    public static String getFirstNumericColumn(Connection connection, TableLocation table) throws SQLException {
        return getFirstNumericColumn(connection, table.toString());
    }

    /**
     * Returns the first numeric column name of a table.
     *
     * @param connection Active connection to the database
     * @param tableName  a table name in the form CATALOG.SCHEMA.TABLE
     * @return The first numeric column name .
     * @throws SQLException If jdbc throws an error
     */
    public static String getFirstNumericColumn(Connection connection, String tableName) throws SQLException {
        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableName + " LIMIT 0;");
            try {
                return getFirstNumericColumn(resultSet.getMetaData());
            } finally {
                resultSet.close();
            }
        } finally {
            statement.close();
        }
    }


    /**
     * Return true is the SQL type is a numeric data type
     *
     * @param sqlType SQL type from {@link java.sql.Types}
     * @return True if the type is numeric
     */
    public static boolean isNumeric(int sqlType) {
        switch (sqlType) {
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.BIGINT:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.INTEGER:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.FLOAT:
                return true;
            default:
                return false;
        }
    }


    /**
     * Returns the first autoincremented column name of a table.
     *
     * @param connection Active connection to the database
     * @param tableLocation  a table name in the form CATALOG.SCHEMA.TABLE
     * @return The first numeric column name .
     * @throws SQLException If jdbc throws an error
     */
    public static String getFirstAutoIncrementColumn(Connection connection, TableLocation tableLocation) throws SQLException {
        if (!tableExists(connection, tableLocation)) {
            throw new SQLException("Table " + tableLocation + " not found.");
        }
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        try (ResultSet columns = databaseMetaData.getColumns(null, null, tableLocation.getTable(), null)) {
            while (columns.next()) {
                if(columns.getBoolean("IS_AUTOINCREMENT")){
                    return columns.getString("COLUMN_NAME");
                }
            }
        }
        return null;
    }


    /**
     * Returns the first autoincremented column name of a table.
     *
     * @param connection Active connection to the database
     * @param tableName  a table name in the form CATALOG.SCHEMA.TABLE
     * @return The first numeric column name .
     * @throws SQLException If jdbc throws an error
     */
    public static String getFirstAutoIncrementColumn(Connection connection, String tableName) throws SQLException {
        if (!tableExists(connection, tableName)) {
            throw new SQLException("Table " + tableName + " not found.");
        }
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        try (ResultSet columns = databaseMetaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {
                if(columns.getBoolean("IS_AUTOINCREMENT")){
                    return columns.getString("COLUMN_NAME");
                }
            }
        }
        return null;
    }
}
