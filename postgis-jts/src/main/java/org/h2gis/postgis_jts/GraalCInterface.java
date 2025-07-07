package org.h2gis.postgis_jts;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Native C interface to access H2GIS using GraalVM.
 * This class exposes simplified C-accessible static methods to:
 * - Open and close JDBC connections
 * - Execute SELECT and UPDATE queries
 * - Fetch results in CSV format
 * - Manage resources (statements, result sets)
 */
public class GraalCInterface {

    /** Map of open JDBC connections, indexed by handle */
    private static final Map<Long, Connection> connections = new ConcurrentHashMap<>();

    /** Map of open JDBC statements, indexed by handle */
    private static final Map<Long, Statement> statements = new ConcurrentHashMap<>();

    /** Map of active JDBC result sets, indexed by handle */
    private static final Map<Long, ResultSet> results = new ConcurrentHashMap<>();

    /** Global handle counter to uniquely identify Java objects from native code */
    private static final AtomicLong handleCounter = new AtomicLong(1);

    /**
     * Opens a connection to an H2 database using the given file path and credentials.
     * @param thread Current isolate thread
     * @param filePathPointer Path to the database (e.g., /tmp/db)
     * @param usernamePointer Username for authentication
     * @param passwordPointer Password for authentication
     * @return A non-zero connection handle on success, 0 on failure
     */
    @CEntryPoint(name = "h2gis_connect")
    public static long h2gisConnect(IsolateThread thread,
                                    CCharPointer filePathPointer,
                                    CCharPointer usernamePointer,
                                    CCharPointer passwordPointer) {
        try {
            String filePath = CTypeConversion.toJavaString(filePathPointer);
            String username = CTypeConversion.toJavaString(usernamePointer);
            String password = CTypeConversion.toJavaString(passwordPointer);

            String url = "jdbc:h2:" + filePath + ";USER=" + username + ";PASSWORD=" + password;
            Properties properties = new Properties();
            Connection conn = org.h2.Driver.load().connect(url, properties);
            long handle = handleCounter.getAndIncrement();
            connections.put(handle, conn);
            return handle;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Executes a SQL SELECT query.
     * @param thread Current isolate thread
     * @param connectionHandle A valid connection handle
     * @param queryPointer SQL query as C string
     * @return A handle to the query result, or 0 on failure
     */
    @CEntryPoint(name = "h2gis_execute")
    public static long h2gisExecute(IsolateThread thread,
                                    long connectionHandle,
                                    CCharPointer queryPointer) {
        try {
            String query = CTypeConversion.toJavaString(queryPointer);
            Connection conn = connections.get(connectionHandle);
            if (conn == null) return 0;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            long queryHandle = handleCounter.getAndIncrement();
            statements.put(queryHandle, stmt);
            results.put(queryHandle, rs);
            return queryHandle;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Fetches the next row from a query result set, formatted as a CSV string.
     * @param thread Current isolate thread
     * @param queryHandle Handle to an active result set
     * @return The next row in CSV format, or an empty string if no more rows
     */
    @CEntryPoint(name = "h2gis_fetch_row")
    public static CCharPointer h2gisFetchRow(IsolateThread thread, long queryHandle) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) return CTypeConversion.toCString("").get();

            if (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    String val = rs.getString(i);
                    if (val != null && (val.contains(",") || val.contains("\n") || val.contains("\""))) {
                        val = val.replace("\"", "\"\"");
                        val = "\"" + val + "\"";
                    }
                    sb.append(val != null ? val : "");
                    if (i < colCount) sb.append(",");
                }
                return CTypeConversion.toCString(sb.toString()).get();
            } else {
                return CTypeConversion.toCString("").get();
            }
        } catch (Exception e) {
            return CTypeConversion.toCString("Error: " + e.toString()).get();
        }
    }

    /**
     * Closes an active query and releases associated resources.
     * @param thread Current isolate thread
     * @param queryHandle Handle to the query
     */
    @CEntryPoint(name = "h2gis_close_query")
    public static void h2gisCloseQuery(IsolateThread thread, long queryHandle) {
        try {
            ResultSet rs = results.remove(queryHandle);
            if (rs != null) rs.close();
            Statement stmt = statements.remove(queryHandle);
            if (stmt != null) stmt.close();
        } catch (Exception ignored) {}
    }

    /**
     * Closes a database connection and releases resources.
     * @param thread Current isolate thread
     * @param connectionHandle Handle to the connection
     */
    @CEntryPoint(name = "h2gis_close_connection")
    public static void h2gisCloseConnection(IsolateThread thread, long connectionHandle) {
        try {
            Connection conn = connections.remove(connectionHandle);
            if (conn != null) conn.close();
        } catch (Exception ignored) {}
    }

    /**
     * Executes a SELECT query and returns all results as a CSV string.
     * The connection is identified by a handle.
     */
    @CEntryPoint(name = "h2gis_query")
    public static CCharPointer h2gisQuery(IsolateThread thread,
                                          long connectionHandle,
                                          CCharPointer queryPointer) {
        Connection conn = connections.get(connectionHandle);
        if (conn == null) {
            return CTypeConversion.toCString("Error: Invalid connection handle").get();
        }
        String query = CTypeConversion.toJavaString(queryPointer);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            StringBuilder sb = new StringBuilder();

            // Header
            for (int i = 1; i <= columnCount; i++) {
                sb.append(metaData.getColumnLabel(i));
                if (i < columnCount) sb.append(",");
            }
            sb.append("\n");

            // Rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String val = rs.getString(i);
                    if (val != null && (val.contains(",") || val.contains("\"") || val.contains("\n"))) {
                        val = val.replace("\"", "\"\"");
                        val = "\"" + val + "\"";
                    }
                    sb.append(val != null ? val : "");
                    if (i < columnCount) sb.append(",");
                }
                sb.append("\n");
            }
            return CTypeConversion.toCString(sb.toString()).get();
        } catch (SQLException e) {
            return CTypeConversion.toCString("Error: " + e.getMessage()).get();
        }
    }

    /**
     * Executes an UPDATE/INSERT/DELETE statement.
     * @param thread Current isolate thread
     * @param connectionHandle Connection handle
     * @param queryPointer SQL command
     * @return Number of rows affected, or -1 if error
     */
    @CEntryPoint(name = "h2gis_execute_update")
    public static int h2gisExecuteUpdate(IsolateThread thread,
                                         long connectionHandle,
                                         CCharPointer queryPointer) {
        try {
            String query = CTypeConversion.toJavaString(queryPointer);
            Connection conn = connections.get(connectionHandle);
            if (conn == null) return -1;

            try (Statement stmt = conn.createStatement()) {
                return stmt.executeUpdate(query);
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Executes a one-off query (connect, execute, close) and returns results as a CSV string.
     * Does not store any connection or statement.
     */
    @CEntryPoint(name = "query_postgis_h2")
    public static CCharPointer queryPostGISH2(
            IsolateThread thread,
            CCharPointer filePathPointer,
            CCharPointer usernamePointer,
            CCharPointer passwordPointer,
            CCharPointer queryPointer
    ) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String filePath = CTypeConversion.toJavaString(filePathPointer);
            String username = CTypeConversion.toJavaString(usernamePointer);
            String password = CTypeConversion.toJavaString(passwordPointer);
            String query = CTypeConversion.toJavaString(queryPointer);

            String url = "jdbc:h2:" + filePath + ";USER=" + username + ";PASSWORD=" + password;
            Properties properties = new Properties();

            connection = org.h2.Driver.load().connect(url, properties);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            StringBuilder sb = new StringBuilder();

            // Header
            for (int i = 1; i <= columnCount; i++) {
                sb.append(metaData.getColumnLabel(i));
                if (i < columnCount) sb.append(",");
            }
            sb.append("\n");

            // Rows
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = resultSet.getString(i);
                    sb.append(value != null ? value : "");
                    if (i < columnCount) sb.append(",");
                }
                sb.append("\n");
            }

            return CTypeConversion.toCString(sb.toString()).get();

        } catch (Exception e) {
            return CTypeConversion.toCString("Error: " + e.toString()).get();
        } finally {
            try { if (resultSet != null) resultSet.close(); } catch (Exception ignored) {}
            try { if (statement != null) statement.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }
    }
}
