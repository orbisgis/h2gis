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

import org.h2gis.api.ProgressVisitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DBMS should follow standard but it is not always the case, this class do some
 * common operations. Compatible with H2 and PostgreSQL.
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class JDBCUtilities {

    public enum FUNCTION_TYPE {
        ALL, BUILT_IN, ALIAS
    }
    public static final String H2_DRIVER_NAME = "H2 JDBC Driver";
    public static final String H2_DRIVER_PACKAGE_NAME = "org.h2.jdbc";

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
     * Return true if table tableName contains field fieldName.
     *
     * @param connection Connection
     * @param tableName Table name
     * @param fieldName Field name
     * @return True if the table contains the field
     * @throws SQLException
     */
    public static boolean hasField(Connection connection, String tableName, String fieldName) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM " + TableLocation.parse(tableName) + " WHERE 1=0;")) {
            return hasField(resultSet.getMetaData(), fieldName);
        } catch (SQLException ex) {
            return false;
        }
    }

    private static boolean hasField(ResultSetMetaData resultSetMetaData, String fieldName) throws SQLException {
        return getFieldIndex(resultSetMetaData, fieldName) != -1;
    }

    /**
     * Fetch the metadata, and check field name
     *
     * @param resultSetMetaData Active result set meta data.
     * @param fieldName Field name, ignore case
     * @return The field index [1-n]; -1 if the field is not found
     * @throws SQLException
     */
    public static int getFieldIndex(ResultSetMetaData resultSetMetaData, String fieldName) throws SQLException {
        for (int columnId = 1; columnId <= resultSetMetaData.getColumnCount(); columnId++) {
            if (resultSetMetaData.getColumnName(columnId).equalsIgnoreCase(fieldName)) {
                return columnId;
            }
        }
        return -1;
    }

    /**
     * @param resultSetMetaData
     * @param fieldIndex Field ordinal position [1-n]
     * @return The field name, empty if the field position or table is not found
     * @throws SQLException If jdbc throws an error
     */
    public static String getFieldName(ResultSetMetaData resultSetMetaData, int fieldIndex) throws SQLException {
        for (int columnId = 1; columnId <= resultSetMetaData.getColumnCount(); columnId++) {
            if (columnId == fieldIndex) {
                return resultSetMetaData.getColumnName(columnId);
            }
        }
        return "";
    }

    /**
     * Returns ResultSetMetaData of a table.
     *
     * @param connection Active connection.
     * @param tableLocation Table identifier [[catalog.]schema.]table
     * @return The list of field names with their indexes.
     * @throws SQLException If jdbc throws an error
     */
    public static ResultSetMetaData getResultSetMetaData(Connection connection, TableLocation tableLocation) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM " + tableLocation + " WHERE 1=0;");
        return resultSet.getMetaData();
    }

    /**
     * Returns the list of all the field name of a table.
     *
     * @param connection Active connection.
     * @param tableLocation Table identifier [[catalog.]schema.]table
     * @return The list of field names with their indexes.
     * @throws SQLException If jdbc throws an error
     */
    public static List<Tuple<String, Integer>> getFieldNamesAndIndexes(Connection connection, TableLocation tableLocation) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM " + tableLocation + " WHERE 1=0;")) {
            return getFieldNamesAndIndexes(resultSet.getMetaData());
        } catch (SQLException ex) {
            throw new SQLException("Cannot retrieve any geometry fields on " + tableLocation);
        }
    }

    /**
     * Returns the list of all the field name of a table.
     *
     * @param resultSetMetaData metadata of a Resulset
     * @return The list of field names with their indexes.
     * @throws SQLException If jdbc throws an error
     */
    public static List<Tuple<String, Integer>> getFieldNamesAndIndexes(ResultSetMetaData resultSetMetaData) throws SQLException {
        ArrayList<Tuple<String, Integer>> namesWithIndexes = new ArrayList<>();
        int columnCount = resultSetMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            namesWithIndexes.add(new Tuple<>(resultSetMetaData.getColumnName(i), i));
        }
        return namesWithIndexes;
    }

    /**
     * Fetch the row count of a table.
     *
     * @param connection Active connection.
     * @param location Table location
     * @return Row count
     * @throws SQLException If the table does not exists, or sql request fail.
     */
    public static int getRowCount(Connection connection, TableLocation location) throws SQLException {
        return getRowCount(connection, location.toString());
    }

    /**
     * Fetch the row count of a table.
     *
     * @param connection Active connection.
     * @param tableReference Table reference
     * @return Row count
     * @throws SQLException If the table does not exists, or sql request fail.
     */
    public static int getRowCount(Connection connection, String tableReference) throws SQLException {
        Statement st = connection.createStatement();
        int rowCount = 0;
        try {
            try (ResultSet rs = st.executeQuery(String.format("select count(*) rowcount from %s", TableLocation.parse(tableReference)))) {
                if (rs.next()) {
                    rowCount = rs.getInt(1);
                }
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
     * @param connection Active connection not closed by this method
     * @param tableReference Table reference
     * @return True if the provided table is temporary.
     * @throws SQLException If the table does not exists.
     */
    public static boolean isTemporaryTable(Connection connection, String tableReference) throws SQLException {
        TableLocation location = TableLocation.parse(tableReference);
        ResultSet rs = getTablesView(connection, location.getCatalog(), location.getSchema(), location.getTable());
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
                throw new SQLException("The table " + location + " does not exists");
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
     * @param tableReference Table reference
     * @return True if the provided table is linked.
     * @throws SQLException If the table does not exists.
     */
    public static boolean isLinkedTable(Connection connection, String tableReference) throws SQLException {
        TableLocation location = TableLocation.parse(tableReference);
        ResultSet rs = getTablesView(connection, location.getCatalog(), location.getSchema(), location.getTable());
        boolean isLinked;
        try {
            if (rs.next()) {
                String tableType = rs.getString("TABLE_TYPE");
                isLinked = tableType.contains("TABLE LINK");
            } else {
                throw new SQLException("The table " + location + " does not exists");
            }
        } finally {
            rs.close();
        }
        return isLinked;
    }

    /**
     * @param connection to the
     * @return True if the provided metadata is a h2 database connection.
     * @throws SQLException
     */
    public static boolean isH2DataBase(Connection connection) throws SQLException {
        return connection.getClass().getName().startsWith(H2_DRIVER_PACKAGE_NAME);
    }

    /**
     * @param metaData Database meta data
     * @return True if the provided metadata is a h2 database connection.
     * @throws SQLException
     */
    public static boolean isH2DataBase(DatabaseMetaData metaData) throws SQLException {
        return metaData.getDriverName().equals(H2_DRIVER_NAME);
    }

    /**
     * @param connection Connection
     * @param tableReference table identifier
     * @return The integer primary key used for edition[1-n]; null if the source
     * is closed or if the table has no primary key or more than one column as
     * primary key
     */
    public static Tuple<String, Integer> getPrimaryKeyNameAndIndex(Connection connection, String tableReference) throws SQLException {        
        TableLocation tableLocation = TableLocation.parse(tableReference);
        if (!tableExists(connection, tableLocation)) {
            throw new SQLException("Table " + tableReference + " not found.");
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
     * @param connection Connection
     * @param tableReference table identifier
     * @return The integer primary key used for edition[1-n]; 0 if the source is
     * closed or if the table has no primary key or more than one column as
     * primary key
     */
    public static int getIntegerPrimaryKey(Connection connection, String tableReference) throws SQLException {        
        TableLocation tableLocation = TableLocation.parse(tableReference);
        if (!tableExists(connection, tableLocation)) {
            throw new SQLException("Table " + tableReference + " not found.");
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
     * Return true if the table exists.
     *
     * @param connection Connection
     * @param tableLocation
     * @return true if the table exists
     * @throws java.sql.SQLException
     */
    public static boolean tableExists(Connection connection, TableLocation tableLocation) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SELECT * FROM " + tableLocation + " WHERE 1=0;");
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

   

    /**
     * Returns the list of table names.
     *
     * @param metaData Database meta data
     * @param catalog A catalog name. Must match the catalog name as it is
     * stored in the database. "" retrieves those without a catalog; null means
     * that the catalog name should not be used to narrow the search
     * @param schemaPattern A schema name pattern. Must match the schema name as
     * it is stored in the database. "" retrieves those without a schema. null
     * means that the schema name should not be used to narrow the search
     * @param tableNamePattern A table name pattern. Must match the table name
     * as it is stored in the database
     * @param types A list of table types, which must be from the list of table
     * types returned from getTableTypes(), to include. null returns all types
     * @return The integer primary key used for edition[1-n]; 0 if the source is
     * closed or if the table has no primary key or more than one column as
     * primary key
     */
    public static List<String> getTableNames(DatabaseMetaData metaData, String catalog, String schemaPattern,
            String tableNamePattern, String[] types) throws SQLException {
        List<String> tableList = new ArrayList<String>();
        ResultSet rs = metaData.getTables(catalog, schemaPattern, tableNamePattern, types);
        boolean isH2 = isH2DataBase(metaData);
        try {
            while (rs.next()) {
                tableList.add(new TableLocation(rs).toString(isH2));
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
     * @param tableName Name of the table containing the field.
     * @param fieldName Name of the field containing the values.
     * @return The list of distinct values of the field.
     */
    public static List<String> getUniqueFieldValues(Connection connection, String tableName, String fieldName) throws SQLException {
        final Statement statement = connection.createStatement();
        List<String> fieldValues = new ArrayList<String>();
        try {
            ResultSet result = statement.executeQuery("SELECT DISTINCT " + TableLocation.quoteIdentifier(fieldName) + " FROM " + TableLocation.parse(tableName));
            while (result.next()) {
                fieldValues.add(result.getString(1));
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
     * @param tableReference Table name
     * @throws java.sql.SQLException
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
     * @throws SQLException
     */
    public static List<String> getFieldNames(ResultSetMetaData resultSetMetaData) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        int cols = resultSetMetaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            columnNames.add(resultSetMetaData.getColumnName(i));
        }
        return columnNames;
    }

    /**
     *
     * @param st Statement to cancel
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
}
