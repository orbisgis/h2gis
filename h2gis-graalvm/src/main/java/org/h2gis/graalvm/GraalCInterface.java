/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.graalvm;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.UnmanagedMemory;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CLongPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.WordBase;
import org.graalvm.word.WordFactory;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.utilities.JDBCUtilities;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C entry points to use H2GIS through JDBC inside a GraalVM native image.
 * <p>
 * Connections, statements and result sets are identified by {@code long}
 * handles (0 = error). On error the functions return 0 or -1 and the message
 * is kept, per thread, for {@code h2gis_get_last_error}.
 * <p>
 * Memory: buffers and strings returned to the caller are allocated with
 * {@link UnmanagedMemory} and must be freed with {@code h2gis_free_result_buffer}
 * (or its alias {@code h2gis_free_string}). Sizes are written as 64 bits
 * integers to the {@code bufferSize} pointers.
 * <p>
 * The format of the result buffers is described in {@link ResultSetWrapper},
 * {@link ColumnWrapper} and {@link TypeCodes}.
 *
 * @author Maël PHILIPPE, CNRS (2025)
 * @author Erwan BOCHER, CNRS (2025-2026)
 */
public class GraalCInterface {

    /**
     * Version of the C interface, returned by {@code h2gis_version}.
     * <ul>
     * <li>2: null masks, type codes 9-12, malloc'd strings for last error and
     * metadata, bind functions returning a status, h2gis_bind_null,
     * h2gis_add_batch / h2gis_execute_batch, spatial functions loaded for
     * in-memory databases.</li>
     * </ul>
     * (Libraries without {@code h2gis_version} are version 1.)
     */
    public static final int C_API_VERSION = 2;

    private static final Logger LOGGER = Logger.getLogger(GraalCInterface.class.getName());

    /** handle -> JDBC connection */
    private static final Map<Long, Connection> connections = new ConcurrentHashMap<>();

    /** handle -> JDBC statement (plain statements of queries, and prepared statements) */
    private static final Map<Long, Statement> statements = new ConcurrentHashMap<>();

    /** handle -> JDBC result set */
    private static final Map<Long, ResultSet> results = new ConcurrentHashMap<>();

    /** result set handle -> handle of the prepared statement that produced it */
    private static final Map<Long, Long> resultOwners = new ConcurrentHashMap<>();

    private static final AtomicLong handleCounter = new AtomicLong(1);

    /** Last error of the current thread, read by h2gis_get_last_error() */
    private static final ThreadLocal<String> lastError = new ThreadLocal<>();

    private static final Object connectLock = new Object();

    private static final String SPATIAL_CHECK = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.ROUTINES "
            + "WHERE ROUTINE_SCHEMA = 'PUBLIC' AND ROUTINE_NAME = 'ST_ASTEXT'";

    static {
        try {
            DriverManager.registerDriver(new org.h2.Driver());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to register H2 Driver", e);
        }
    }

    // ------------------------------------------------------------------
    // Version, errors and memory
    // ------------------------------------------------------------------

    /**
     * @param thread the current isolate thread
     * @return the version of this C interface ({@link #C_API_VERSION})
     */
    @CEntryPoint(name = "h2gis_version")
    public static int h2gisVersion(IsolateThread thread) {
        return C_API_VERSION;
    }

    /**
     * Last error message of the current thread, cleared once read.
     *
     * @param thread the current isolate thread
     * @return a UTF-8 string ("" if no error) to free with h2gis_free_string
     */
    @CEntryPoint(name = "h2gis_get_last_error")
    public static CCharPointer h2gisGetLastError(IsolateThread thread) {
        String error = lastError.get();
        lastError.remove();
        return toNativeString(error == null ? "" : error);
    }

    /**
     * Free a buffer or a string returned by this library.
     *
     * @param thread the current isolate thread
     * @param ptr    pointer returned by a h2gis_* function (0 is ignored)
     */
    @CEntryPoint(name = "h2gis_free_result_buffer")
    public static void freeResultBuffer(IsolateThread thread, WordBase ptr) {
        if (ptr.rawValue() != 0L) {
            UnmanagedMemory.free((CCharPointer) ptr);
        }
    }

    /**
     * Alias of h2gis_free_result_buffer, for strings.
     *
     * @param thread the current isolate thread
     * @param ptr    string returned by a h2gis_* function
     */
    @CEntryPoint(name = "h2gis_free_string")
    public static void freeString(IsolateThread thread, WordBase ptr) {
        freeResultBuffer(thread, ptr);
    }

    // ------------------------------------------------------------------
    // Connections
    // ------------------------------------------------------------------

    /**
     * Open a connection; the H2GIS functions are loaded if missing.
     *
     * @param thread          the current isolate thread
     * @param filePathPointer database path, appended to "jdbc:h2:"
     * @param usernamePointer user name
     * @param passwordPointer password
     * @return a connection handle, or 0 on error
     */
    @CEntryPoint(name = "h2gis_connect")
    public static long h2gisConnect(IsolateThread thread, CCharPointer filePathPointer,
                                    CCharPointer usernamePointer, CCharPointer passwordPointer) {
        if (filePathPointer.isNull() || usernamePointer.isNull() || passwordPointer.isNull()) {
            setError("Null pointer received in connection parameters", null);
            return 0;
        }
        try {
            Properties properties = new Properties();
            properties.setProperty("url", "jdbc:h2:" + toJavaString(filePathPointer));
            properties.setProperty("user", toJavaString(usernamePointer));
            properties.setProperty("password", toJavaString(passwordPointer));
            Connection connection;
            synchronized (connectLock) {
                // Don't let the factory load the functions: it does it on a
                // temporary connection, and closing it destroys a "mem:" database.
                connection = JDBCUtilities.wrapSpatialDataSource(
                        H2GISDBFactory.createDataSource(properties, false)).getConnection();
                try {
                    loadSpatialFunctionsIfMissing(connection);
                } catch (SQLException e) {
                    connection.close();
                    throw e;
                }
            }
            long handle = handleCounter.getAndIncrement();
            connections.put(handle, connection);
            return handle;
        } catch (Exception e) {
            setError("Failed to open H2 connection", e);
            return 0;
        }
    }

    private static void loadSpatialFunctionsIfMissing(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(SPATIAL_CHECK)) {
            if (rs.next() && rs.getLong(1) > 0) {
                return;
            }
        }
        H2GISFunctions.load(connection);
    }

    /**
     * Load the H2GIS functions in the database.
     *
     * @param thread           the current isolate thread
     * @param connectionHandle connection handle
     * @return 1 on success, 0 on error
     */
    @CEntryPoint(name = "h2gis_load")
    public static long h2gisLoad(IsolateThread thread, long connectionHandle) {
        Connection conn = connection(connectionHandle);
        if (conn == null) {
            return 0;
        }
        try {
            H2GISFunctions.load(conn);
            return 1;
        } catch (Exception e) {
            setError("Failed to load H2GIS functions", e);
            return 0;
        }
    }

    /**
     * Close a connection and every statement and result set still open on it.
     *
     * @param thread           the current isolate thread
     * @param connectionHandle connection handle
     */
    @CEntryPoint(name = "h2gis_close_connection")
    public static void h2gisCloseConnection(IsolateThread thread, long connectionHandle) {
        Connection conn = connections.remove(connectionHandle);
        if (conn == null) {
            setError("Invalid or already closed connection handle: " + connectionHandle, null);
            return;
        }
        forgetHandlesOf(conn);
        try {
            conn.close();
        } catch (Exception e) {
            setError("Failed to close connection", e);
        }
    }

    /**
     * Drop all the objects, delete the database files and close the connection.
     *
     * @param thread           the current isolate thread
     * @param connectionHandle connection handle
     */
    @CEntryPoint(name = "h2gis_delete_database_and_close")
    public static void h2gisDeleteClose(IsolateThread thread, long connectionHandle) {
        Connection conn = connections.remove(connectionHandle);
        if (conn == null) {
            setError("Invalid or already closed connection handle: " + connectionHandle, null);
            return;
        }
        forgetHandlesOf(conn);
        try {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP ALL OBJECTS DELETE FILES");
            }
            conn.close();
        } catch (Exception e) {
            setError("Failed to delete and close database", e);
        }
    }

    // ------------------------------------------------------------------
    // Plain statements
    // ------------------------------------------------------------------

    /**
     * Run a query.
     *
     * @param thread           the current isolate thread
     * @param connectionHandle connection handle
     * @param queryPointer     SQL query
     * @return a result set handle, or 0 on error
     */
    @CEntryPoint(name = "h2gis_fetch")
    public static long h2gisFetch(IsolateThread thread, long connectionHandle, CCharPointer queryPointer) {
        if (queryPointer.isNull()) {
            setError("Null pointer received for query", null);
            return 0;
        }
        Connection conn = connection(connectionHandle);
        if (conn == null) {
            return 0;
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(toJavaString(queryPointer));
            long handle = handleCounter.getAndIncrement();
            statements.put(handle, stmt);
            results.put(handle, rs);
            return handle;
        } catch (Exception e) {
            closeQuietly(stmt);
            setError("Failed to execute query", e);
            return 0;
        }
    }

    /**
     * Run an update statement (or a script of several statements).
     *
     * @param thread           the current isolate thread
     * @param connectionHandle connection handle
     * @param queryPointer     SQL statement
     * @return the update count, or -1 on error
     */
    @CEntryPoint(name = "h2gis_execute")
    public static int h2gisExecute(IsolateThread thread, long connectionHandle, CCharPointer queryPointer) {
        if (queryPointer.isNull()) {
            setError("Null pointer received for update query", null);
            return -1;
        }
        Connection conn = connection(connectionHandle);
        if (conn == null) {
            return -1;
        }
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(toJavaString(queryPointer));
        } catch (Exception e) {
            setError("Failed to execute update", e);
            return -1;
        }
    }

    // ------------------------------------------------------------------
    // Prepared statements
    // ------------------------------------------------------------------

    /**
     * Prepare a statement with JDBC "?" placeholders.
     *
     * @param thread           the current isolate thread
     * @param connectionHandle connection handle
     * @param sqlPointer       SQL statement
     * @return a statement handle, or 0 on error
     */
    @CEntryPoint(name = "h2gis_prepare")
    public static long h2gisPrepare(IsolateThread thread, long connectionHandle, CCharPointer sqlPointer) {
        if (sqlPointer.isNull()) {
            setError("Null pointer for SQL", null);
            return 0;
        }
        Connection conn = connection(connectionHandle);
        if (conn == null) {
            return 0;
        }
        try {
            PreparedStatement pstmt = conn.prepareStatement(toJavaString(sqlPointer),
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            long handle = handleCounter.getAndIncrement();
            statements.put(handle, pstmt);
            return handle;
        } catch (Exception e) {
            setError("Prepare failed", e);
            return 0;
        }
    }

    /** @return 0 on success, -1 on error (bind functions) */
    @CEntryPoint(name = "h2gis_bind_double")
    public static int h2gisBindDouble(IsolateThread thread, long stmtHandle, int index, double value) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            ps.setDouble(index, value);
            return 0;
        } catch (Exception e) {
            setError("Bind double failed", e);
            return -1;
        }
    }

    /** @return 0 on success, -1 on error */
    @CEntryPoint(name = "h2gis_bind_int")
    public static int h2gisBindInt(IsolateThread thread, long stmtHandle, int index, int value) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            ps.setInt(index, value);
            return 0;
        } catch (Exception e) {
            setError("Bind int failed", e);
            return -1;
        }
    }

    /** @return 0 on success, -1 on error */
    @CEntryPoint(name = "h2gis_bind_long")
    public static int h2gisBindLong(IsolateThread thread, long stmtHandle, int index, long value) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            ps.setLong(index, value);
            return 0;
        } catch (Exception e) {
            setError("Bind long failed", e);
            return -1;
        }
    }

    /**
     * Bind a string; a NULL pointer binds SQL NULL.
     *
     * @return 0 on success, -1 on error
     */
    @CEntryPoint(name = "h2gis_bind_string")
    public static int h2gisBindString(IsolateThread thread, long stmtHandle, int index, CCharPointer value) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            ps.setString(index, value.isNull() ? null : toJavaString(value));
            return 0;
        } catch (Exception e) {
            setError("Bind string failed", e);
            return -1;
        }
    }

    /**
     * Bind bytes (EWKB is accepted for GEOMETRY parameters).
     *
     * @return 0 on success, -1 on error
     */
    @CEntryPoint(name = "h2gis_bind_blob")
    public static int h2gisBindBlob(IsolateThread thread, long stmtHandle, int index, CCharPointer value, int len) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            if (value.isNull()) {
                ps.setNull(index, Types.VARBINARY);
            } else {
                byte[] bytes = new byte[len];
                CTypeConversion.asByteBuffer(value, len).get(bytes);
                ps.setBytes(index, bytes);
            }
            return 0;
        } catch (Exception e) {
            setError("Bind blob failed", e);
            return -1;
        }
    }

    /**
     * Bind SQL NULL.
     *
     * @return 0 on success, -1 on error
     */
    @CEntryPoint(name = "h2gis_bind_null")
    public static int h2gisBindNull(IsolateThread thread, long stmtHandle, int index) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            ps.setNull(index, Types.NULL);
            return 0;
        } catch (Exception e) {
            setError("Bind null failed", e);
            return -1;
        }
    }

    /**
     * Run a prepared query.
     *
     * @param thread     the current isolate thread
     * @param stmtHandle statement handle
     * @return a result set handle, or 0 on error
     */
    @CEntryPoint(name = "h2gis_execute_prepared")
    public static long h2gisExecutePrepared(IsolateThread thread, long stmtHandle) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return 0;
        }
        try {
            // a new execution closes the previous result set of the statement
            forgetResultsOf(stmtHandle);
            ResultSet rs = ps.executeQuery();
            long rsHandle = handleCounter.getAndIncrement();
            results.put(rsHandle, rs);
            resultOwners.put(rsHandle, stmtHandle);
            return rsHandle;
        } catch (Exception e) {
            setError("Execute prepared failed", e);
            return 0;
        }
    }

    /**
     * Run a prepared update.
     *
     * @param thread     the current isolate thread
     * @param stmtHandle statement handle
     * @return the update count, or -1 on error
     */
    @CEntryPoint(name = "h2gis_execute_prepared_update")
    public static int h2gisExecutePreparedUpdate(IsolateThread thread, long stmtHandle) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            return ps.executeUpdate();
        } catch (Exception e) {
            setError("Execute prepared update failed", e);
            return -1;
        }
    }

    /**
     * Add the current parameters to the batch of a prepared statement.
     *
     * @param thread     the current isolate thread
     * @param stmtHandle statement handle
     * @return 0 on success, -1 on error
     */
    @CEntryPoint(name = "h2gis_add_batch")
    public static int h2gisAddBatch(IsolateThread thread, long stmtHandle) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            ps.addBatch();
            return 0;
        } catch (Exception e) {
            setError("Add batch failed", e);
            return -1;
        }
    }

    /**
     * Run the batch of a prepared statement.
     *
     * @param thread     the current isolate thread
     * @param stmtHandle statement handle
     * @return the total update count, or -1 on error (the batch is cleared)
     */
    @CEntryPoint(name = "h2gis_execute_batch")
    public static long h2gisExecuteBatch(IsolateThread thread, long stmtHandle) {
        PreparedStatement ps = prepared(stmtHandle);
        if (ps == null) {
            return -1;
        }
        try {
            long total = 0;
            for (int count : ps.executeBatch()) {
                if (count > 0) {
                    total += count;
                }
            }
            return total;
        } catch (Exception e) {
            try {
                ps.clearBatch();
            } catch (SQLException ignored) {
                // the error of the batch is the one to report
            }
            setError("Execute batch failed", e);
            return -1;
        }
    }

    // ------------------------------------------------------------------
    // Results
    // ------------------------------------------------------------------

    /**
     * Close a result set, a statement or a prepared statement handle. Closing
     * a prepared statement also closes its result sets.
     *
     * @param thread      the current isolate thread
     * @param queryHandle handle
     */
    @CEntryPoint(name = "h2gis_close_query")
    public static void h2gisCloseQuery(IsolateThread thread, long queryHandle) {
        resultOwners.remove(queryHandle);
        closeQuietly(results.remove(queryHandle));
        forgetResultsOf(queryHandle);
        closeQuietly(statements.remove(queryHandle));
    }

    /**
     * Close a result set but keep its handle (prefer h2gis_close_query).
     *
     * @param thread      the current isolate thread
     * @param queryHandle result set handle
     * @return 0 on success, 1 on error
     */
    @CEntryPoint(name = "h2gis_free_result_set")
    public static long freeResultResultSet(IsolateThread thread, long queryHandle) {
        ResultSet rs = results.get(queryHandle);
        if (rs == null) {
            setError("Invalid result set handle: " + queryHandle, null);
            return 1;
        }
        try {
            rs.close();
            return 0;
        } catch (Exception e) {
            setError("Failed to close result set", e);
            return 1;
        }
    }

    /**
     * Serialize all the remaining rows.
     *
     * @param thread      the current isolate thread
     * @param queryHandle result set handle
     * @param bufferSize  pointer to a 64 bits integer receiving the buffer size
     * @return the buffer (free with h2gis_free_result_buffer), or 0 on error
     */
    @CEntryPoint(name = "h2gis_fetch_all")
    public static WordBase h2gisFetchAll(IsolateThread thread, long queryHandle, WordBase bufferSize) {
        return fetch(queryHandle, -1, bufferSize, "h2gis_fetch_all");
    }

    /**
     * Serialize the next row (a buffer with 0 rows at the end).
     *
     * @param thread      the current isolate thread
     * @param queryHandle result set handle
     * @param bufferSize  pointer to a 64 bits integer receiving the buffer size
     * @return the buffer (free with h2gis_free_result_buffer), or 0 on error
     */
    @CEntryPoint(name = "h2gis_fetch_one")
    public static WordBase h2gisFetchOne(IsolateThread thread, long queryHandle, WordBase bufferSize) {
        return fetch(queryHandle, 1, bufferSize, "h2gis_fetch_one");
    }

    /**
     * Serialize up to {@code batchSize} rows (less rows means the end is reached).
     *
     * @param thread      the current isolate thread
     * @param queryHandle result set handle
     * @param batchSize   maximum number of rows
     * @param bufferSize  pointer to a 64 bits integer receiving the buffer size
     * @return the buffer (free with h2gis_free_result_buffer), or 0 on error
     */
    @CEntryPoint(name = "h2gis_fetch_batch")
    public static WordBase h2gisFetchBatch(IsolateThread thread, long queryHandle, int batchSize, WordBase bufferSize) {
        return fetch(queryHandle, Math.max(0, batchSize), bufferSize, "h2gis_fetch_batch");
    }

    private static WordBase fetch(long queryHandle, int maxRows, WordBase bufferSize, String what) {
        writeLong(bufferSize, 0L);
        ResultSet rs = results.get(queryHandle);
        if (rs == null) {
            setError(what + ": invalid result set handle " + queryHandle, null);
            return WordFactory.zero();
        }
        try {
            byte[] buffer = ResultSetWrapper.from(rs, maxRows).serialize();
            return toNativeBuffer(buffer, bufferSize);
        } catch (Throwable e) {
            setError(what + " failed", e);
            return WordFactory.zero();
        }
    }

    /**
     * Type code of each column (int32 array, see {@link TypeCodes}).
     *
     * @param thread      the current isolate thread
     * @param queryHandle result set handle
     * @param colCountOut pointer to a 64 bits integer receiving the column count
     * @return the array (free with h2gis_free_result_buffer), or 0 on error
     */
    @CEntryPoint(name = "h2gis_get_column_types")
    public static WordBase h2gisGetColumnTypes(IsolateThread thread, long queryHandle, WordBase colCountOut) {
        writeLong(colCountOut, 0L);
        ResultSet rs = results.get(queryHandle);
        if (rs == null) {
            setError("h2gis_get_column_types: invalid result set handle " + queryHandle, null);
            return WordFactory.zero();
        }
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            byte[] arr = new byte[colCount * 4];
            java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(arr).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            for (int i = 1; i <= colCount; i++) {
                bb.putInt(TypeCodes.of(meta, i));
            }
            writeLong(colCountOut, colCount);
            return colCount == 0 ? WordFactory.zero() : toNativeBuffer(arr, WordFactory.zero());
        } catch (Exception e) {
            setError("h2gis_get_column_types failed", e);
            return WordFactory.zero();
        }
    }

    /**
     * Column metadata as JSON: {@code [{"name":..., "typeName":..., "type":...}, ...]}.
     *
     * @param thread      the current isolate thread
     * @param queryHandle result set handle
     * @return a UTF-8 string ("[]" on error) to free with h2gis_free_string
     */
    @CEntryPoint(name = "h2gis_get_metadata_json")
    public static CCharPointer h2gisGetMetadataJson(IsolateThread thread, long queryHandle) {
        ResultSet rs = results.get(queryHandle);
        if (rs == null) {
            setError("h2gis_get_metadata_json: invalid result set handle " + queryHandle, null);
            return toNativeString("[]");
        }
        try {
            ResultSetMetaData meta = rs.getMetaData();
            StringBuilder json = new StringBuilder("[");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (i > 1) {
                    json.append(',');
                }
                json.append("{\"name\":");
                appendJsonString(json, meta.getColumnLabel(i));
                json.append(",\"typeName\":");
                appendJsonString(json, meta.getColumnTypeName(i));
                json.append(",\"type\":").append(TypeCodes.of(meta, i)).append('}');
            }
            return toNativeString(json.append(']').toString());
        } catch (Exception e) {
            setError("Failed to get metadata json", e);
            return toNativeString("[]");
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Connection connection(long handle) {
        Connection conn = connections.get(handle);
        if (conn == null) {
            setError("Invalid connection handle: " + handle, null);
        }
        return conn;
    }

    private static PreparedStatement prepared(long handle) {
        Statement s = statements.get(handle);
        if (s instanceof PreparedStatement) {
            return (PreparedStatement) s;
        }
        setError("Invalid prepared statement handle: " + handle, null);
        return null;
    }

    private static void forgetResultsOf(long stmtHandle) {
        resultOwners.entrySet().removeIf(entry -> {
            if (entry.getValue() == stmtHandle) {
                closeQuietly(results.remove(entry.getKey()));
                return true;
            }
            return false;
        });
    }

    /** Drop the handles of the statements and result sets of a closed connection. */
    private static void forgetHandlesOf(Connection conn) {
        statements.entrySet().removeIf(entry -> {
            try {
                if (entry.getValue().getConnection() == conn) {
                    long handle = entry.getKey();
                    closeQuietly(results.remove(handle));
                    forgetResultsOf(handle);
                    closeQuietly(entry.getValue());
                    return true;
                }
            } catch (SQLException ignored) {
                // closed statement: drop it
                return true;
            }
            return false;
        });
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // nothing to report: the handle is gone anyway
            }
        }
    }

    /** Decode a NUL terminated C string as UTF-8 (whatever the default charset). */
    private static String toJavaString(CCharPointer pointer) {
        int length = 0;
        while (pointer.read(length) != 0) {
            length++;
        }
        return CTypeConversion.toJavaString(pointer, WordFactory.unsigned(length), StandardCharsets.UTF_8);
    }

    /** Copy {@code data} to a new unmanaged buffer and write its size to {@code sizeOut}. */
    private static WordBase toNativeBuffer(byte[] data, WordBase sizeOut) {
        CCharPointer addr = UnmanagedMemory.malloc(Math.max(1, data.length));
        CTypeConversion.asByteBuffer(addr, data.length).put(data);
        writeLong(sizeOut, data.length);
        return addr;
    }

    /** A NUL terminated UTF-8 copy of {@code text} in unmanaged memory. */
    private static CCharPointer toNativeString(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        CCharPointer addr = UnmanagedMemory.malloc(bytes.length + 1);
        CTypeConversion.asByteBuffer(addr, bytes.length).put(bytes);
        addr.write(bytes.length, (byte) 0);
        return addr;
    }

    private static void writeLong(WordBase address, long value) {
        if (address.rawValue() != 0L) {
            ((CLongPointer) WordFactory.pointer(address.rawValue())).write(value);
        }
    }

    private static void appendJsonString(StringBuilder sb, String s) {
        sb.append('"');
        if (s != null) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if (c < 0x20) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                }
            }
        }
        sb.append('"');
    }

    /**
     * Keep the error for h2gis_get_last_error. Errors are expected by the
     * callers (they get the message), so they are only logged at FINE level.
     */
    private static void setError(String message, Throwable e) {
        String text = e == null ? message : message + ": " + (e.getMessage() != null ? e.getMessage() : e.toString());
        lastError.set(text);
        LOGGER.log(Level.FINE, message, e);
    }
}
