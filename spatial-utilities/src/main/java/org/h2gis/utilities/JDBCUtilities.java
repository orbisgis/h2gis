package org.h2gis.utilities;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * DBMS should follow standard but it is not always the case, this class do some common operations.
 * Compatible with H2 and PostgreSQL.
 * @author Nicolas Fortin
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class JDBCUtilities {
    public enum FUNCTION_TYPE { ALL, BUILT_IN, ALIAS}
    public static final String H2_DRIVER_NAME = "H2 JDBC Driver";

    private JDBCUtilities() {}

    private static ResultSet getTablesView(Connection connection, String catalog, String schema, String table) throws SQLException {
        Integer catalogIndex = null;
        Integer schemaIndex = null;
        Integer tableIndex = 1;
        StringBuilder sb = new StringBuilder("SELECT * from INFORMATION_SCHEMA.TABLES where ");
        if(!catalog.isEmpty()) {
            sb.append("UPPER(table_catalog) = ? AND ");
            catalogIndex = 1;
            tableIndex++;
        }
        if(!schema.isEmpty()) {
            sb.append("UPPER(table_schema) = ? AND ");
            schemaIndex = tableIndex;
            tableIndex++;
        }
        sb.append("UPPER(table_name) = ? ");
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
    private static boolean hasField(ResultSetMetaData resultSetMetaData, String fieldName) throws SQLException {
        return getFieldIndex(resultSetMetaData, fieldName) != -1;
    }

    /**
     * Fetch the metadata, and check field name
     * @param resultSetMetaData Active result set meta data.
     * @param fieldName Field name, ignore case
     * @return The field index [1-n]; -1 if the field is not found
     * @throws SQLException
     */
    public static int getFieldIndex(ResultSetMetaData resultSetMetaData, String fieldName) throws SQLException {
        for(int columnId = 1; columnId <= resultSetMetaData.getColumnCount(); columnId++) {
            if(fieldName.equalsIgnoreCase(resultSetMetaData.getColumnName(columnId))) {
                return columnId;
            }
        }
        return -1;
    }

    /**
     * @param meta DataBase meta data
     * @param table Table identifier [[catalog.]schema.]table
     * @param fieldIndex Field ordinal position [1-n]
     * @return The field name, empty if the field position or table is not found
     * @throws SQLException If jdbc throws an error
     */
    public static String getFieldName(DatabaseMetaData meta, String table, int fieldIndex) throws SQLException {
        TableLocation location = TableLocation.parse(table);
        ResultSet rs = meta.getColumns(location.getCatalog(null), location.getSchema(null), location.getTable(), null);
        try {
            while(rs.next()) {
                if(rs.getInt("ORDINAL_POSITION") == fieldIndex) {
                    return rs.getString("COLUMN_NAME");
                }
            }
        } finally {
            rs.close();
        }
        return "";
    }

    /**
     * Fetch the row count of a table.
     * @param connection Active connection.
     * @param tableReference Table reference
     * @return Row count
     * @throws SQLException If the table does not exists, or sql request fail.
     */
    public static int getRowCount(Connection connection, String tableReference) throws SQLException {
        Statement st = connection.createStatement();
        int rowCount = 0;
        try {
            ResultSet rs = st.executeQuery(String.format("select count(*) rowcount from %s", TableLocation.parse(tableReference)));
            try {
                if(rs.next()) {
                    rowCount = rs.getInt(1);
                }
            } finally {
                rs.close();
            }
        }finally {
            st.close();
        }
        return rowCount;
    }

    /**
     * Read INFORMATION_SCHEMA.TABLES in order to see if the provided table reference is a temporary table.
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
            if(rs.next()) {
                String tableType;
                if(hasField(rs.getMetaData(), "STORAGE_TYPE")) {
                    // H2
                    tableType = rs.getString("STORAGE_TYPE");
                } else {
                    // Standard SQL
                    tableType = rs.getString("TABLE_TYPE");
                }
                isTemporary = tableType.contains("TEMPORARY");
            } else {
                throw new SQLException("The table "+location+" does not exists");
            }
        } finally {
            rs.close();
        }
        return isTemporary;
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
     * @return The integer primary key used for edition[1-n]; 0 if the source is closed or if the table has no primary
     *         key or more than one column as primary key
     */
    public static int getIntegerPrimaryKey(Connection connection, String tableReference) throws SQLException {
        if (!tableExists(connection, tableReference)) {
            throw new SQLException("Table " + tableReference + " not found.");
        }
        final DatabaseMetaData meta = connection.getMetaData();
        TableLocation tableLocation = TableLocation.parse(tableReference);
        String columnNamePK = null;
        ResultSet rs = meta.getPrimaryKeys(tableLocation.getCatalog(null), tableLocation.getSchema(null),
                tableLocation.getTable());
        try {
            while (rs.next()) {
                // If the schema is not specified, public must be the schema
                if(!tableLocation.getSchema().isEmpty() || "public".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
                    if(columnNamePK == null) {
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
                    if(!tableLocation.getSchema().isEmpty() || "public".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
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
     * @param tableName  Table name
     * @return true if the table exists
     */
    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        final Statement statement = connection.createStatement();
        try {
            statement.execute("SELECT * FROM " +
                    TableLocation.parse(tableName, JDBCUtilities.isH2DataBase(connection.getMetaData())) +
                    " LIMIT 0;");
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            statement.close();
        }
    }
}
