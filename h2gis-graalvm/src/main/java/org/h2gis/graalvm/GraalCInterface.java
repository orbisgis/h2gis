package org.h2gis.graalvm;

import org.graalvm.nativeimage.IsolateThread;
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
import java.nio.ByteOrder;
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

    /** Unsafe is used for direct memory allocation/free to pass buffers to native side */
    private static final Unsafe unsafe = getUnsafe();


    static {
        try {
            // Ensure H2 driver is registered exactly once
            DriverManager.registerDriver(new org.h2.Driver());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to register H2 Driver", e);
        }
    }

    /**
     * Retrieves the last error message encountered by the current thread.
     * The message is cleared after retrieval.
     * @param thread the current Graal Isolate thread
     * @return C string pointer to the last error message or empty string if none
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
     * Opens a new connection to an H2GIS database.
     * @param thread the current Graal Isolate thread
     * @param filePathPointer C pointer to the database file path string
     * @param usernamePointer C pointer to the database username string
     * @param passwordPointer C pointer to the database password string
     * @return a unique non-zero handle representing the connection, or 0 on failure
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
     * Loads the H2GIS spatial functions into the connected database.
     * @param thread the current Graal Isolate thread
     * @param connectionHandle handle representing an active connection
     * @return 1 on success, 0 on error
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
     * Executes a SELECT SQL query and stores the resulting Statement and ResultSet.
     * @param thread the current Graal Isolate thread
     * @param connectionHandle handle representing an active connection
     * @param queryPointer C pointer to the SQL SELECT query string
     * @return a unique handle for the query result set, or 0 on failure
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
     * Executes an UPDATE/INSERT/DELETE SQL query.
     * @param thread the current Graal Isolate thread
     * @param connectionHandle handle representing an active connection
     * @param queryPointer C pointer to the SQL update query string
     * @return the number of rows affected, or -1 on failure
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
     * Closes a previously opened query result set and associated statement.
     * @param thread the current Graal Isolate thread
     * @param queryHandle handle representing the query to close
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
     * Closes a previously opened connection.
     * @param thread the current Graal Isolate thread
     * @param connectionHandle handle representing the connection to close
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
     * Deletes all objects in the database and closes the connection.
     * @param thread the current Graal Isolate thread
     * @param connectionHandle handle representing the connection to delete/close
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
                ResultSet rs = stmt.executeQuery("SELECT DATABASE()");
                if (rs.next()) {
                    String dbName = rs.getString(1);
                    stmt.executeUpdate("DROP DATABASE `" + dbName + "`");
                }else{
                    throw new SQLException("Could not find database name");
                }
                rs.close();

            }

            conn.close();
            connections.remove(connectionHandle);
        } catch (Exception e) {
            logAndSetError("Failed to delete and close database", e);
        }
    }


    /**
     * Fetches all remaining rows from the result set of a query and returns them
     * in a JSON-encoded native memory buffer.
     * @param thread the current Graal Isolate thread
     * @param queryHandle handle representing the query result set
     * @param bufferSize pointer to store the size of the returned memory buffer
     * @return native memory pointer to the JSON buffer, or 0 on error
     */
    @CEntryPoint(name = "h2gis_fetch_all")
    public static WordBase h2gisFetchAll(IsolateThread thread, long queryHandle, WordBase bufferSize) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {

                if (bufferSize.rawValue() != 0L) {
                    unsafe.putLong(bufferSize.rawValue(), 0L);
                }
                return WordFactory.zero();
            }
            ResultSetWrapper resultSetWrapper = ResultSetWrapper.from(rs);

            byte[] arr = resultSetWrapper.serialize();

            // Écrire le nombre de lignes dans la mémoire pointée par rowNum, si valide
            if (bufferSize.rawValue() != 0L) {
                unsafe.putLong(bufferSize.rawValue(), arr.length);
            }

            // Allouer la mémoire native pour le buffer
            long addr = unsafe.allocateMemory(arr.length);

            // Copier les données dans la mémoire native allouée
            for (int i = 0; i < arr.length; i++) {
                unsafe.putByte(addr + i, arr[i]);
            }

            rs.close();

            // Retourner un pointeur vers cette mémoire (WordBase)
            return WordFactory.pointer(addr);

        } catch (Exception e) {
            System.err.println("Error in h2gis_fetch_all: " + e.getMessage());
            if (bufferSize.rawValue() != 0L) {
                unsafe.putLong(bufferSize.rawValue(), 0L);
            }
            return WordFactory.zero();
        }
    }


    /**
     * Fetches the first row from the result set of a query and returns it
     * in an encoded native memory buffer.
     * @param thread the current Graal Isolate thread
     * @param queryHandle handle representing the query result set
     * @param bufferSize pointer to store the size of the returned memory buffer
     * @return native memory pointer to the JSON buffer, or 0 on error
     */
    @CEntryPoint(name = "h2gis_fetch_one")
    public static WordBase h2gisFetchOne(IsolateThread thread, long queryHandle, WordBase bufferSize) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {

                if (bufferSize.rawValue() != 0L) {
                    unsafe.putLong(bufferSize.rawValue(), 0L);
                }
                return WordFactory.zero();
            }
            ResultSetWrapper resultSetWrapper = ResultSetWrapper.fromOne(rs);

            byte[] arr = resultSetWrapper.serialize();

            // Write the buffer size
            if (bufferSize.rawValue() != 0L) {
                unsafe.putLong(bufferSize.rawValue(), arr.length);
            }

            // Allocate memory for the buffer
            long addr = unsafe.allocateMemory(arr.length);

            // Copy data in newly allocated memory
            for (int i = 0; i < arr.length; i++) {
                unsafe.putByte(addr + i, arr[i]);
            }

            // return a pointer on this buffer-dedicated memory
            return WordFactory.pointer(addr);

        } catch (Exception e) {
            System.err.println("Error in h2gis_fetch_all: " + e.getMessage());
            if (bufferSize.rawValue() != 0L) {
                unsafe.putLong(bufferSize.rawValue(), 0L);
            }
            return WordFactory.zero();
        }
    }

    /**
     * Retrieves the type of each column in a query result set.
     * Types are returned as an array of integers encoded in native memory.
     * The encoding uses little-endian order with 4 bytes per type code.
     * @param thread the current Graal Isolate thread
     * @param queryHandle handle representing the query result set
     * @param colCountOut native pointer where the column count will be stored (can be 0)
     * @return a native memory pointer to an array of type codes, or 0 on error
     */
    @CEntryPoint(name = "h2gis_get_column_types")
    public static WordBase h2gisGetColumnTypes(IsolateThread thread, long queryHandle, WordBase colCountOut) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {
                if (colCountOut.rawValue() != 0L) {
                    unsafe.putLong(colCountOut.rawValue(), 0L);
                }
                return WordFactory.zero();
            }

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            if (colCount < 1) {
                if (colCountOut.rawValue() != 0L) {
                    unsafe.putLong(colCountOut.rawValue(), 0L);
                }
                return WordFactory.zero();
            }

            ByteBuffer buffer = ByteBuffer.allocate((colCount) * 4).order(ByteOrder.LITTLE_ENDIAN);


            for (int i = 1; i <= colCount; i++) {
                String sqlTypeName = meta.getColumnTypeName(i).toLowerCase();
                int jdbcType = meta.getColumnType(i);
                int typeCode;

                switch (jdbcType) {
                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        typeCode = 1; // INT
                        break;
                    case Types.BIGINT:
                        typeCode = 2; // LONG
                        break;
                    case Types.FLOAT:
                    case Types.REAL:
                        typeCode = 3; // FLOAT (32-bit)
                        break;
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        typeCode = 4; // DOUBLE (64-bit)
                        break;
                    case Types.BOOLEAN:
                    case Types.BIT:
                        typeCode = 5; // BOOLEAN
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                        typeCode = 6; // STRING
                        break;
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        typeCode = 7; // DATE as string
                        break;
                    case Types.OTHER:
                    case Types.STRUCT:
                    default:
                        if (sqlTypeName.startsWith("geometry")) {
                            typeCode = 8; // GEOMETRY (WKB)
                        } else {
                            typeCode = 99; // OTHER (as string)
                        }
                        break;
                }

                buffer.putInt(typeCode);
            }


            byte[] arr = buffer.array();

            if (colCountOut.rawValue() != 0L) {
                unsafe.putLong(colCountOut.rawValue(), colCount);
            }

            // Copy to native memory
            long addr = unsafe.allocateMemory(arr.length);
            for (int i = 0; i < arr.length; i++) {
                unsafe.putByte(addr + i, arr[i]);
            }

            return WordFactory.pointer(addr);

        } catch (Exception e) {
            System.err.println("Error in h2gis_get_column_types: " + e.getMessage());
            return WordFactory.zero();
        }
    }


    /**
     * Frees a previously stored query result set, closing its resources.
     * @param thread the current Graal Isolate thread
     * @param queryHandle handle representing the query result set to free
     * @return 0 on success, 1 on error
     */
    @CEntryPoint(name = "h2gis_free_result_set")
    public static long freeResultResultSet(IsolateThread thread, long queryHandle) {
        try {
            results.get(queryHandle).close();
            return 0;
        } catch (SQLException e) {
            System.err.println(e.toString());
            return 1;
        }
    }


    /**
     * Frees a result buffer previously allocated by h2gis_fetch_rows or h2gis_get_column_types.
     * @param thread the current Graal Isolate thread
     * @param ptr native pointer to the buffer to free
     */
    @CEntryPoint(name = "h2gis_free_result_buffer")
    public static void freeResultBuffer(IsolateThread thread, WordBase ptr) {
        if (ptr.rawValue() != 0L) {
            unsafe.freeMemory(ptr.rawValue());
        }
    }


    /**
     * Logs an error and sets it in the thread-local error variable.
     * @param message the error message to log
     * @param e the exception that was thrown, or null
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
     * @return the Unsafe instance
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
