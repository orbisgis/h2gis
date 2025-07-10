/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.graalvm;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.utilities.JDBCUtilities;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Native C interface to access H2GIS via GraalVM native-image.
 * Provides functions for connection management, query execution, result retrieval and cleanup.
 */
public class GraalCInterface {

    private static final Logger LOGGER = Logger.getLogger(GraalCInterface.class.getName());
    private static final Map<Long, Connection> connections = new ConcurrentHashMap<>();
    private static final Map<Long, Statement> statements = new ConcurrentHashMap<>();
    private static final Map<Long, ResultSet> results = new ConcurrentHashMap<>();
    private static final AtomicLong handleCounter = new AtomicLong(1);
    private static final ThreadLocal<String> lastError = new ThreadLocal<>();

    static {
        try {
            DriverManager.registerDriver(new org.h2.Driver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get last error as a C string. Cleared after read.
     */
    @CEntryPoint(name = "h2gis_get_last_error")
    public static CCharPointer h2gisGetLastError(IsolateThread thread) {
        String error = lastError.get();
        lastError.remove();
        if (error == null) error = "";
        try (CTypeConversion.CCharPointerHolder holder = CTypeConversion.toCString(error)) {
            return holder.get();
        }
    }

    /**
     * Opens a JDBC connection to an H2 database.
     *
     * @param thread    Isolate thread (unused, but required by GraalVM)
     * @param filePathPointer C string path to the H2 database file
     * @param usernamePointer C string username
     * @param passwordPointer C string password
     * @return Non-zero handle if successful, 0 on failure
     */
    @CEntryPoint(name = "h2gis_connect")
    public static long h2gisConnect(IsolateThread thread,
                                    CCharPointer filePathPointer,
                                    CCharPointer usernamePointer,
                                    CCharPointer passwordPointer) {




        // Null pointer checks for safety
        if (filePathPointer.isNull() || usernamePointer.isNull() || passwordPointer.isNull()) {
            logAndSetError("Null pointer received in connection parameters", null);
            return 0;
        }


        try {
            // Convert C strings to Java strings
            String filePath = CTypeConversion.toJavaString(filePathPointer);
            String username = CTypeConversion.toJavaString(usernamePointer);
            String password = CTypeConversion.toJavaString(passwordPointer);

            // Form JDBC connection
            String url = "jdbc:h2:" + filePath;
            Properties properties = new Properties();
            properties.setProperty("url", url);
            properties.setProperty("user", username);
            properties.setProperty("password", password);

            Connection connection = JDBCUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(properties)).getConnection();

            long handle = handleCounter.getAndIncrement();
            connections.put(handle, connection);

            return handle;
        } catch (Exception e) {
            logAndSetError("Failed to open H2 connection", e);
            return 0;
        }
    }

    /**
     * Load H2GIS spatial functions into the database connection.
     * @return 1 on success, 0 on failure
     */
    @CEntryPoint(name = "h2gis_load")
    public static long h2gisLoad(IsolateThread thread, long connectionHandle) {
        try {
            Connection conn = connections.get(connectionHandle);
            if (conn == null) {
                logAndSetError("Invalid connection handle: " + connectionHandle, null);
                return 0;
            }
            H2GISFunctions.load(conn);
            return 1;
        } catch (Exception e) {
            logAndSetError("Failed to load H2GIS functions", e);
            return 0;
        }
    }

    /**
     * Executes a SQL query and stores the result.
     *
     * @param thread          Isolate thread
     * @param connectionHandle Handle to a valid JDBC connection
     * @param queryPointer    C string containing the SQL query
     * @return Non-zero handle to the result set, or 0 on failure
     */
    @CEntryPoint(name = "h2gis_execute")
    public static long h2gisExecute(IsolateThread thread, long connectionHandle, CCharPointer queryPointer) {
        if (queryPointer.isNull()) {
            logAndSetError("Null pointer received for query", null);
            return 0;
        }

        try {
            String query = CTypeConversion.toJavaString(queryPointer);
            Connection conn = connections.get(connectionHandle);
            if (conn == null) {
                logAndSetError("Invalid connection handle: " + connectionHandle, null);
                return 0;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            long handle = handleCounter.getAndIncrement();
            statements.put(handle, stmt);
            results.put(handle, rs);
            return handle;
        } catch (Exception e) {
            logAndSetError("Failed to execute query", e);
            return 0;
        }
    }

    /**
     * Execute a SQL update (INSERT, UPDATE, DELETE).
     * @return number of affected rows, or -1 on failure
     */
    @CEntryPoint(name = "h2gis_execute_update")
    public static int h2gisExecuteUpdate(IsolateThread thread, long connectionHandle, CCharPointer queryPointer) {
        if (queryPointer.isNull()) {
            logAndSetError("Null pointer received for update query", null);
            return -1;
        }

        Connection conn = connections.get(connectionHandle);
        if (conn == null) {
            logAndSetError("Invalid connection handle: " + connectionHandle, null);
            return -1;
        }

        String query = CTypeConversion.toJavaString(queryPointer);
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(query);
        } catch (Exception e) {
            logAndSetError("Failed to execute update", e);
            return -1;
        }
    }

    /**
     * Closes a resultset and its associated statement, identified by queryHandle.
     * @param thread Isolate thread
     * @param queryHandle Handle to the resultset
     */
    @CEntryPoint(name = "h2gis_close_query")
    public static void h2gisCloseQuery(IsolateThread thread, long queryHandle) {
        ResultSet rs = results.remove(queryHandle);
        Statement stmt = statements.remove(queryHandle);
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (Exception e) {
            logAndSetError("Failed to close query resources", e);
        }
    }

    /**
     * Close a database connection.
     */
    @CEntryPoint(name = "h2gis_close_connection")
    public static void h2gisCloseConnection(IsolateThread thread, long connectionHandle) {
        Connection conn = connections.remove(connectionHandle);
        try {
            if (conn != null) {
                conn.close();
            } else {
                logAndSetError("Invalid or already closed connection handle: " + connectionHandle, null);
            }
        } catch (Exception e) {
            logAndSetError("Failed to close connection", e);
        }
    }

    /**
     * Delete all objects and files of the database, then close it.
     */
    @CEntryPoint(name = "h2gis_delete_database_and_close")
    public static void h2gisDeleteClose(IsolateThread thread, long connectionHandle) {
        Connection conn = connections.get(connectionHandle);
        try {
            if (conn == null) {
                logAndSetError("Invalid or already closed connection handle: " + connectionHandle, null);
                return;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(30);
                stmt.executeUpdate("DROP ALL OBJECTS DELETE FILES");
            }

            conn.close();
            connections.remove(connectionHandle);
        } catch (Exception e) {
            logAndSetError("Failed to delete and close database", e);
        }
    }

    /**
     * Fetch the next row from a result set as a CSV string.
     * Returns an empty string when all rows have been fetched.
     */
    @CEntryPoint(name = "h2gis_fetch_rows")
    public static CCharPointer h2gisFetchRows(IsolateThread thread, long queryHandle) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {
                return emptyCString();
            }

            if (rs.next()) {
                String jsonRow = formatRowAsJson(rs);
                try (CTypeConversion.CCharPointerHolder holder = CTypeConversion.toCString(jsonRow)) {
                    return holder.get();
                }
            } else {
                return emptyCString();
            }
        } catch (Exception e) {
            logAndSetError("Failed to fetch row", e);
            try (CTypeConversion.CCharPointerHolder holder = CTypeConversion.toCString("Error: " + e.getMessage())) {
                return holder.get();
            }
        }
    }

    private static CCharPointer emptyCString() {
        try (CTypeConversion.CCharPointerHolder holder = CTypeConversion.toCString("")) {
            return holder.get();
        }
    }

    private static String formatRowAsJson(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for (int i = 1; i <= colCount; i++) {
            String columnName = meta.getColumnLabel(i);
            Object value = rs.getObject(i);

            sb.append("\"").append(escapeJson(columnName)).append("\":");

            if (value == null) {
                sb.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value.toString());
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }

            if (i < colCount) sb.append(",");
        }

        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    private static void logAndSetError(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
        lastError.set(message + (throwable != null ? ": " + throwable.getMessage() : ""));
    }
}
