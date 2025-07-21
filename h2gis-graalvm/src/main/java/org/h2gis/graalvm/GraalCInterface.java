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
import java.util.Map;
import java.util.Properties;
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


    /** Logger for internal error reporting */
    private static final Logger LOGGER = Logger.getLogger(GraalCInterface.class.getName());

    /** Maps handle -> JDBC connection */
    private static final Map<Long, Connection> connections = new ConcurrentHashMap<>();

    /** Maps handle -> JDBC statement */
    private static final Map<Long, Statement> statements = new ConcurrentHashMap<>();

    /** Maps handle -> JDBC result set */
    private static final Map<Long, ResultSet> results = new ConcurrentHashMap<>();

    /** Handle counter to assign unique IDs to each resource */
    private static final AtomicLong handleCounter = new AtomicLong(1);

    /** Thread-local error message, retrieved by C with h2gis_get_last_error() */
    private static final ThreadLocal<String> lastError = new ThreadLocal<>();

    /** GraalVM ObjectHandles (not used currently, but useful for future object passing) */
    public static final ObjectHandles handles = ObjectHandles.getGlobal();

    /** Unsafe is used for direct memory allocation/free to pass buffers to native side */
    private static final Unsafe unsafe = getUnsafe();

    /** Cached "null" byte array for efficient reuse */
    private static final byte[] NULL_BYTES = "null".getBytes(StandardCharsets.UTF_8);


    static {
        try {
            // Ensure H2 driver is registered exactly once
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
        if (error == null){
            error = "";
        }
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
            if (rs != null){
                rs.close();
            }
        } catch (Exception e) {
            logAndSetError("Failed to close ResultSet", e);
        }
        try {
            if (stmt != null){
                stmt.close();
            }
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
     * Native C entry point for fetching all remaining rows from a previously executed query,
     * converting the result set to a JSON-formatted byte array allocated in native memory.
     *
     * This method returns a JSON array of arrays in a bytebuffer, where:
     * - The first element is an array of column names.
     * - Each subsequent element is an array of row values.
     *
     * Data encoding follows JSON rules:
     * - Null values are encoded as the literal null.
     * - Strings and geometry values are escaped and quoted as JSON strings.
     * - Numbers and booleans are encoded directly without quotes.
     * - Binary values (fallback) are encoded as JSON strings.
     *
     * Example output:
     * [["id","name","geom"], [1,"Park","POINT(1 2)"], [2,"Lake","POINT(3 4)"]]
     *
     * The resulting JSON is written into a freshly allocated native memory block
     * whose pointer is returned. The size in bytes of the allocated buffer is
     * written to the `sizeOutPtr` address (if not null).
     *
     * The caller is responsible for freeing the memory returned via the pointer.
     *
     * On failure, returns a null pointer (0) and sets size to 0.
     *
     * @param thread the current Graal isolate thread (required for native interoperability)
     * @param queryHandle the unique handle identifying the ResultSet to read from
     * @param sizeOutPtr pointer to a memory location where the result size in bytes will be written
     * @return a native memory pointer to the encoded JSON result, or null (0) on error
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
            putByte(buffer, (byte) '[');
            putByte(buffer, (byte) '[');

            boolean firstColumn = true;
            for (int col = 1; col <= colCount; col++) {//we write the column array
                if (!firstColumn) {
                    putByte(buffer, (byte) ',');
                }
                firstColumn = false;
                // Write column name as JSON key (with quotes and escaping)
                String colName = meta.getColumnName(col);
                buffer = putJsonString(buffer, colName);
            }

            putByte(buffer, (byte) ']');

            while (rs.next()) {//for each row
                putByte(buffer, (byte) ',');
                putByte(buffer, (byte) '[');

                firstColumn = true;
                for (int col = 1; col <= colCount; col++) {//parse the rows and write the values in the buffer
                    if (!firstColumn) {
                        putByte(buffer, (byte) ',');
                    }
                    firstColumn = false;
                    // Write column name as JSON key (with quotes and escaping)
                    String colType = meta.getColumnTypeName(col).toUpperCase();
                    boolean isGeometry = colType.startsWith("GEOMETRY");

                    Object value = rs.getObject(col);

                    if (value == null) {
                        // JSON null literal (no quotes)
                        putBytes(buffer, "null".getBytes(StandardCharsets.UTF_8));
                    } else if (isGeometry || value instanceof String) {
                        // Geometry: encode as JSON string
                        buffer = putJsonString(buffer, value.toString());
                    } else if(value instanceof Number || value instanceof Boolean){ //TODO : rajouter les strings aussi
                        putBytes(buffer, value.toString().getBytes(StandardCharsets.UTF_8));
                    }else{
                        byte[] valBytes = rs.getBytes(col);
                        if (valBytes == null) {
                            putBytes(buffer, "null".getBytes(StandardCharsets.UTF_8));
                        } else {
                            buffer = putJsonString(buffer, value.toString());
                        }
                    }
                }

                putByte(buffer, (byte) ']');
            }

            putByte(buffer, (byte) ']');



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
            System.err.println("Error in h2gis_fetch_rows: " + e.getMessage());

            if (sizeOutPtr.rawValue() != 0L) {
                unsafe.putLong(sizeOutPtr.rawValue(), 0L);
            }
            return WordFactory.zero();
        }
    }




    /**
     * Writes a JSON-formatted and escaped string into the given ByteBuffer.
     *
     * The string is wrapped in double quotes and escaped according to JSON rules.
     * This includes:
     * - Escaping special characters like double quotes (") and backslashes (\)
     * - Replacing control characters such as newline, tab, etc. with their escaped equivalents
     * - Encoding characters outside the ASCII printable range using Unicode escape sequences (e.g. \u00E9)
     *
     * This method assumes the ByteBuffer has enough remaining capacity to store
     * the resulting escaped string. No buffer overflow checks are performed.
     *
     * Example:
     * Input string: Hello\n"World"
     * Output in buffer: "Hello\\n\\\"World\\\""
     *
     * @param buffer the ByteBuffer into which the JSON string will be written
     * @param s the raw input string to format as a JSON string
     * @return the same ByteBuffer, with the JSON string written into it
     */
    private static ByteBuffer putJsonString(ByteBuffer buffer, String s) {
        putByte(buffer, (byte) '"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': putBytes(buffer, new byte[]{'\\', '"'}); break;
                case '\\': putBytes(buffer, new byte[]{'\\', '\\'}); break;
                case '\b': putBytes(buffer, new byte[]{'\\', 'b'}); break;
                case '\f': putBytes(buffer, new byte[]{'\\', 'f'}); break;
                case '\n': putBytes(buffer, new byte[]{'\\', 'n'}); break;
                case '\r': putBytes(buffer, new byte[]{'\\', 'r'}); break;
                case '\t': putBytes(buffer, new byte[]{'\\', 't'}); break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        String unicode = String.format("\\u%04x", (int) c);
                        putBytes(buffer, unicode.getBytes(StandardCharsets.UTF_8));
                    } else {
                        putByte(buffer, (byte) c);
                    }
            }
        }
        putByte(buffer, (byte) '"');
        return buffer;
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



    /* Utility methods */

    /**
     * Logs an error and sets it in the thread-local error variable.
     */
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

    /* Helper methods for ByteBuffer manipulation */

    /**
     * Write a bite in a given buffer, and increase the buffer size
     * if it does not have any spalce anymore.
     * @param buffer the buffer in which the byte will be written
     * @param b the byte to write
     */
    private static void putByte(ByteBuffer buffer, byte b) {
        if (buffer.remaining() < 1) {
            buffer = growBuffer(buffer);
        }
        buffer.put(b);
    }

    /**
     * Write abytes into a buffer
     * @param buffer the buffer in which the bytes will be written
     * @param bytes the bytes to write
     */
    private static void putBytes(ByteBuffer buffer, byte[] bytes) {
        for (byte b : bytes) {
            putByte(buffer, b);
        }
    }


    /**
     * Method that increase the size of a given buffer
     * @param buffer the buffer who's size will be increased
     * @return
     */
    private static ByteBuffer growBuffer(ByteBuffer buffer) {
        int newCapacity = buffer.capacity() * 2;
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
