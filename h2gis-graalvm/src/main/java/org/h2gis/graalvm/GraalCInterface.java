package org.h2gis.graalvm;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.hosted.Feature;
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
 * org.h2gis.graalvm.GraalCInterface exposes H2GIS database operations to native code via the GraalVM C interface.
 * It allows connecting to an H2 database, executing SQL queries and updates, retrieving results,
 * and managing resources like connections, statements, and result sets.
 *
 * Handles are used to track resources safely between native and Java code.
 */
public class GraalCInterface {

    // --- Static Fields ---

    /** Logger used for internal error reporting */
    private static final Logger LOGGER = Logger.getLogger(GraalCInterface.class.getName());

    /** Map storing active database connections, referenced by a unique handle */
    private static final Map<Long, Connection> connections = new ConcurrentHashMap<>();

    /** Map storing statements associated with queries, referenced by a handle */
    private static final Map<Long, Statement> statements = new ConcurrentHashMap<>();

    /** Map storing result sets from executed SELECT queries */
    private static final Map<Long, ResultSet> results = new ConcurrentHashMap<>();

    /** Global atomic counter to generate unique handles for connections, statements, and results */
    private static final AtomicLong handleCounter = new AtomicLong(1);

    /** Thread-local error string to store the last error per native thread */
    private static final ThreadLocal<String> lastError = new ThreadLocal<>();

    // --- Public C API Methods (Accessible from native C) ---

    /**
     * Returns the last error message that occurred in the current native thread.
     * The message is cleared after retrieval.
     *
     * @param thread The current isolate thread (required by GraalVM)
     * @return C string containing the error, or an empty string if none
     */
    @CEntryPoint(name = "h2gis_get_last_error")
    public static CCharPointer h2gisGetLastError(IsolateThread thread) {
        String error = lastError.get();
        if (error == null) {
            return CTypeConversion.toCString("").get();
        }
        lastError.remove();  // Clear after retrieval
        return CTypeConversion.toCString(error).get();
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

            //Connection conn = org.h2.Driver.load().connect(url, properties);
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
     * Executes a SQL SELECT query and stores the result.
     *
     * @param thread          Isolate thread
     * @param connectionHandle Handle to a valid JDBC connection
     * @return Non-zero handle to the result set, or 0 on failure
     */
    @CEntryPoint(name = "h2gis_load")
    public static long h2gsLoad(IsolateThread thread,
                                    long connectionHandle) {


        try {
            Connection conn = connections.get(connectionHandle);
            if (conn == null) {
                logAndSetError("Invalid connection handle: " + connectionHandle, null);
                return 0;
            }
            H2GISFunctions.load(conn);

            return 1;
        } catch (Exception e) {
            logAndSetError("Failed to execute query", e);
            return 0;
        }
    }

    /**
     * Executes a SQL SELECT query and stores the result.
     *
     * @param thread          Isolate thread
     * @param connectionHandle Handle to a valid JDBC connection
     * @param queryPointer    C string containing the SQL SELECT query
     * @return Non-zero handle to the result set, or 0 on failure
     */
    @CEntryPoint(name = "h2gis_execute")
    public static long h2gisExecute(IsolateThread thread,
                                    long connectionHandle,
                                    CCharPointer queryPointer) {
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
            stmt.setQueryTimeout(30); // Prevent infinite execution
            ResultSet rs = stmt.executeQuery(query);

            long queryHandle = handleCounter.getAndIncrement();
            statements.put(queryHandle, stmt);
            results.put(queryHandle, rs);
            return queryHandle;
        } catch (Exception e) {
            logAndSetError("Failed to execute query", e);
            return 0;
        }
    }

    /**
     * Executes an SQL UPDATE, INSERT, or DELETE query.
     *
     * @param thread          Isolate thread
     * @param connectionHandle Handle to a valid JDBC connection
     * @param queryPointer    C string containing the SQL update
     * @return Number of rows affected, or -1 on error
     */
    @CEntryPoint(name = "h2gis_execute_update")
    public static int h2gisExecuteUpdate(IsolateThread thread,
                                         long connectionHandle,
                                         CCharPointer queryPointer) {
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
            stmt.setQueryTimeout(30); // Prevent long blocking
            return stmt.executeUpdate(query);
        } catch (Exception e) {
            logAndSetError("Failed to execute update", e);
            return -1;
        }
    }

    /**
     * Closes a result set and its associated statement, identified by queryHandle.
     *
     * @param thread      Isolate thread
     * @param queryHandle Handle to the result set
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
     * Closes a database connection, identified by its handle.
     *
     * @param thread           Isolate thread
     * @param connectionHandle Handle to the connection
     */
    @CEntryPoint(name = "h2gis_close_connection")
    public static void h2gisCloseConnection(IsolateThread thread, long connectionHandle) {
        Connection conn = connections.remove(connectionHandle);
        try {
            if (conn == null) {
                logAndSetError("Attempted to close an invalid or already-closed connection handle: " + connectionHandle, null);
                return;
            }
            conn.close();
        } catch (Exception e) {
            logAndSetError("Failed to close connection", e);
        }
    }

    /**
     * Closes a database connection, and delete the database
     *
     * @param thread           Isolate thread
     * @param connectionHandle Handle to the connection
     */
    @CEntryPoint(name = "h2gis_delete_database_and_close")
    public static void h2gisDeleteClose(IsolateThread thread, long connectionHandle) {
        Connection conn = connections.get(connectionHandle);
        try {
            if (conn == null) {
                logAndSetError("Attempted to close an invalid or already-closed connection handle: " + connectionHandle, null);
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(30); // Prevent long blocking
                stmt.executeUpdate("drop all objects delete files");
            } catch (Exception e) {
                logAndSetError("Failed to execute update", e);
                return;
            }

            conn.close();
            connections.remove(connectionHandle);

        } catch (Exception e) {
            logAndSetError("Failed to close connection", e);
        }
    }

    /**
     * Fetches the next row from a query result set, formatted as a CSV string.
     * @return The next row in CSV format, or an empty string if no more rows.
     */
    @CEntryPoint(name = "h2gis_fetch_row")
    public static CCharPointer h2gisFetchRow(IsolateThread thread, long queryHandle) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {
                // No error log here, as it's valid for a handle to be closed.
                return CTypeConversion.toCString("").get();
            }

            if (rs.next()) {
                String csvRow = formatRowAsCsv(rs);
                return CTypeConversion.toCString(csvRow).get();
            } else {
                return CTypeConversion.toCString("").get(); // End of result set
            }
        } catch (Exception e) {
            logAndSetError("Failed to fetch row", e);
            // Return error message in the data channel as a fallback
            return CTypeConversion.toCString("Error: " + e.getMessage()).get();
        }
    }

    // --- Private Utility Method ---
    /**
     * Formats the current row of a ResultSet into a single CSV string.
     * @param rs The ResultSet, positioned at the row to format.
     * @return A CSV-formatted string.
     * @throws SQLException If a database access error occurs.
     */
    private static String formatRowAsCsv(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= colCount; i++) {
            String val = rs.getString(i);
            if (val != null) {
                // Efficiently check for characters that require quoting
                if (val.indexOf(',') != -1 || val.indexOf('\n') != -1 || val.indexOf('"') != -1) {
                    // Enclose in quotes and escape existing quotes
                    sb.append('"').append(val.replace("\"", "\"\"")).append('"');
                } else {
                    sb.append(val);
                }
            }
            if (i < colCount) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Logs an error to the Java logger and sets the thread-local error string
     * so that native code can retrieve it.
     *
     * @param message   Custom error message
     * @param throwable Optional exception to include
     */
    private static void logAndSetError(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
        lastError.set(message + (throwable != null ? ": " + throwable.getMessage() : ""));
    }
}
