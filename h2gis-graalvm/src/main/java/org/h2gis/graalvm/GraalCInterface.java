package org.h2gis.graalvm;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.ObjectHandles;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.WordBase;
import org.graalvm.word.WordFactory;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.utilities.JDBCUtilities;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.graalvm.nativeimage.c.type.CTypeConversion.toCString;

/**
 * GraalCInterface exposes C entry points to interact with H2GIS through
 * JDBC connections inside a GraalVM native image.
 *
 * This class manages connections, statements, result sets, executes queries,
 * and returns results as raw buffers to native code.
 */
public class GraalCInterface {

    private static final Logger LOGGER = Logger.getLogger(GraalCInterface.class.getName());

    // Maps to track handles for JDBC resources
    private static final Map<Long, Connection> connections = new ConcurrentHashMap<>();
    private static final Map<Long, Statement> statements = new ConcurrentHashMap<>();
    private static final Map<Long, ResultSet> results = new ConcurrentHashMap<>();

    // Atomic handle generator
    private static final AtomicLong handleCounter = new AtomicLong(1);

    // ThreadLocal to store the last error message per thread
    private static final ThreadLocal<String> lastError = new ThreadLocal<>();

    // GraalVM object handles global singleton (used if needed)
    public static final ObjectHandles handles = ObjectHandles.getGlobal();

    // Unsafe for manual memory management (allocate/free native buffers)
    private static final Unsafe unsafe = getUnsafe();

    // Register the H2 Driver statically once
    static {
        try {
            DriverManager.registerDriver(new org.h2.Driver());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to register H2 Driver", e);
        }
    }

    /**
     * Returns the last error message for the current thread as a C string.
     * The returned pointer must be copied on the C side if needed.
     */
    @CEntryPoint(name = "h2gis_get_last_error")
    public static CCharPointer h2gisGetLastError(IsolateThread thread) {
        String error = lastError.get();
        lastError.remove();
        if (error == null) error = "";
        return toCString(error).get();
    }

    /**
     * Opens a connection to an H2 database wrapped with H2GIS spatial functions.
     * Returns a handle (long) or 0 on error.
     */
    @CEntryPoint(name = "h2gis_connect")
    public static long h2gisConnect(IsolateThread thread,
                                    CCharPointer filePathPointer,
                                    CCharPointer usernamePointer,
                                    CCharPointer passwordPointer) {
        if (filePathPointer.isNull() || usernamePointer.isNull() || passwordPointer.isNull()) {
            logAndSetError("Null pointer received in connection parameters", null);
            return 0;
        }

        try {
            String filePath = CTypeConversion.toJavaString(filePathPointer);
            String username = CTypeConversion.toJavaString(usernamePointer);
            String password = CTypeConversion.toJavaString(passwordPointer);

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
     * Loads H2GIS spatial functions into the database connection.
     * Returns 1 on success, 0 on failure.
     */
    @CEntryPoint(name = "h2gis_load")
    public static long h2gisLoad(IsolateThread thread, long connectionHandle) {
        Connection conn = connections.get(connectionHandle);
        if (conn == null) {
            logAndSetError("Invalid connection handle: " + connectionHandle, null);
            return 0;
        }
        try {
            H2GISFunctions.load(conn);
            return 1;
        } catch (Exception e) {
            logAndSetError("Failed to load H2GIS functions", e);
            return 0;
        }
    }

    /**
     * Executes a SQL query and returns a handle to the ResultSet.
     * Also keeps the Statement alive.
     * Returns 0 on failure.
     */
    @CEntryPoint(name = "h2gis_fetch")
    public static long h2gisFetch(IsolateThread thread, long connectionHandle, CCharPointer queryPointer) {
        if (queryPointer.isNull()) {
            logAndSetError("Null pointer received for query", null);
            return 0;
        }

        Connection conn = connections.get(connectionHandle);
        if (conn == null) {
            logAndSetError("Invalid connection handle: " + connectionHandle, null);
            return 0;
        }

        try {
            String query = CTypeConversion.toJavaString(queryPointer);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
     * Executes an update SQL statement (INSERT/UPDATE/DELETE).
     * Returns number of affected rows or -1 on failure.
     */
    @CEntryPoint(name = "h2gis_execute")
    public static int h2gisExecute(IsolateThread thread, long connectionHandle, CCharPointer queryPointer) {
        if (queryPointer.isNull()) {
            logAndSetError("Null pointer received for update query", null);
            return -1;
        }

        Connection conn = connections.get(connectionHandle);
        if (conn == null) {
            logAndSetError("Invalid connection handle: " + connectionHandle, null);
            return -1;
        }

        try {
            String query = CTypeConversion.toJavaString(queryPointer);
            try (Statement stmt = conn.createStatement()) {
                return stmt.executeUpdate(query);
            }
        } catch (Exception e) {
            logAndSetError("Failed to execute update", e);
            return -1;
        }
    }

    /**
     * Closes the statement and result set associated with a query handle.
     */
    @CEntryPoint(name = "h2gis_close_query")
    public static void h2gisCloseQuery(IsolateThread thread, long queryHandle) {
        ResultSet rs = results.remove(queryHandle);
        Statement stmt = statements.remove(queryHandle);
        try {
            if (rs != null) rs.close();
        } catch (Exception e) {
            logAndSetError("Failed to close ResultSet", e);
        }
        try {
            if (stmt != null) stmt.close();
        } catch (Exception e) {
            logAndSetError("Failed to close Statement", e);
        }
    }

    /**
     * Closes a database connection and removes it from the handle map.
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
     * Drops all objects in the database and deletes database files,
     * then closes the connection.
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
     * Fetches all rows from a query handle’s ResultSet and serializes
     * them as a JSON-like buffer allocated off-heap.
     *
     * The length of the buffer is written to the pointer given by sizeOutPtr.
     * Returns a native pointer to the allocated buffer.
     */
    @CEntryPoint(name = "h2gis_fetch_rows")
    public static WordBase h2gisFetchRows(IsolateThread thread, long queryHandle, WordBase sizeOutPtr) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {
                if (sizeOutPtr.rawValue() != 0L) {
                    unsafe.putLong(sizeOutPtr.rawValue(), 0L);
                }
                return WordFactory.zero();
            }

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            ByteBuffer buffer = ByteBuffer.allocate(8192);
            buffer = putByte(buffer, (byte)'{');

            boolean firstColumn = true;

            for (int col = 1; col <= colCount; col++) {
                if (!firstColumn) buffer = putByte(buffer, (byte)',');
                firstColumn = false;

                // Write column name as key: 'colname':
                buffer = putByte(buffer, (byte)'\'');
                byte[] colNameBytes = meta.getColumnName(col).getBytes(StandardCharsets.UTF_8);
                buffer = ensureCapacity(buffer, colNameBytes.length);
                buffer.put(colNameBytes);
                buffer = putBytes(buffer, new byte[]{(byte)'\'' , (byte)':' , (byte)'['});

                String colType = meta.getColumnTypeName(col).toUpperCase();
                boolean isGeometry = colType.startsWith("GEOMETRY");

                boolean firstValue = true;
                rs.beforeFirst();
                while (rs.next()) {
                    if (!firstValue) buffer = putByte(buffer, (byte)',');
                    firstValue = false;

                    buffer = putByte(buffer, (byte)'\'');

                    Object value = rs.getObject(col);
                    byte[] valBytes;

                    if (value == null) {
                        valBytes = new byte[0]; // empty string
                    } else if (isGeometry) {
                        valBytes = value.toString().getBytes(StandardCharsets.UTF_8);
                    } else {
                        valBytes = rs.getBytes(col);
                        if (valBytes == null) valBytes = new byte[0];
                    }

                    buffer = ensureCapacity(buffer, valBytes.length + 1);
                    buffer.put(valBytes);
                    buffer = putByte(buffer, (byte)'\'');
                }

                buffer = putByte(buffer, (byte)']');
            }

            buffer = putByte(buffer, (byte)'}');

            buffer.flip();
            int len = buffer.limit();
            long addr = unsafe.allocateMemory(len);
            for (int i = 0; i < len; i++) {
                unsafe.putByte(addr + i, buffer.get(i));
            }

            if (sizeOutPtr.rawValue() != 0L) {
                unsafe.putLong(sizeOutPtr.rawValue(), len);
            }

            return WordFactory.pointer(addr);

        } catch (Exception e) {
            e.printStackTrace();
            if (sizeOutPtr.rawValue() != 0L) {
                unsafe.putLong(sizeOutPtr.rawValue(), 0L);
            }
            return WordFactory.zero();
        }
    }

    /**
     * Frees a result buffer previously allocated by h2gis_fetch_rows.
     */
    @CEntryPoint(name = "h2gis_free_result_buffer")
    public static void freeResultBuffer(IsolateThread thread, WordBase ptr) {
        if (ptr.rawValue() != 0L) {
            unsafe.freeMemory(ptr.rawValue());
        }
    }

    /* Helper methods for ByteBuffer manipulation */

    private static ByteBuffer putByte(ByteBuffer buffer, byte b) {
        if (buffer.remaining() < 1) {
            buffer = growBuffer(buffer);
        }
        buffer.put(b);
        return buffer;
    }

    private static ByteBuffer putString(ByteBuffer buffer, String str) {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
        for (byte b : utf8Bytes) {
            // Escape backslash and single quote
            if (b == '\\' || b == '\'') {
                buffer = putByte(buffer, (byte) '\\');
            }
            buffer = putByte(buffer, b);
        }
        return buffer;
    }

    private static ByteBuffer putBytes(ByteBuffer buffer, byte[] bytes) {
        for (byte b : bytes) {
            buffer = putByte(buffer, b);
        }
        return buffer;
    }

    private static ByteBuffer ensureCapacity(ByteBuffer buffer, int extra) {
        if (buffer.remaining() < extra) {
            buffer = growBuffer(buffer);
        }
        return buffer;
    }

    private static ByteBuffer growBuffer(ByteBuffer buffer) {
        int newCapacity = buffer.capacity() * 2;
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /* Utility methods */

    private static void logAndSetError(String message, Exception e) {
        if (e != null) {
            LOGGER.log(Level.SEVERE, message, e);
            lastError.set(message + ": " + e.getMessage());
        } else {
            LOGGER.severe(message);
            lastError.set(message);
        }
    }

    /**
     * Uses reflection hack to access the Unsafe instance.
     * Unsafe is used to allocate and free off-heap memory.
     */
    private static Unsafe getUnsafe() {
        try {
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get Unsafe instance", e);
        }
    }
}
