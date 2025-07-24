package org.h2gis.graalvm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    private static ObjectHandles globalHandles = ObjectHandles.getGlobal();


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
                if (sizeOutPtr.rawValue() != 0L) unsafe.putLong(sizeOutPtr.rawValue(), 0L);
                return WordFactory.zero();
            }

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            String[] colNames = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                colNames[i] = meta.getColumnName(i + 1);
            }

            ByteBuffer buffer = ByteBuffer.allocate(32 * 1024);
            ByteBufferBackedOutputStream out = new ByteBufferBackedOutputStream(buffer);

            JsonFactory factory = new JsonFactory();
            JsonGenerator gen = factory.createGenerator(out);
            gen.writeStartArray(); // [
            gen.writeStartArray(); // [

            for (String name : colNames) {
                gen.writeString(name);
            }

            gen.writeEndArray(); // ]

            while (rs.next()) {
                gen.writeStartArray();
                for (int col = 1; col <= colCount; col++) {
                    Object value = rs.getObject(col);
                    if (value == null) {
                        gen.writeNull();
                    } else if (value instanceof Number) {
                        if (value instanceof Integer) gen.writeNumber((Integer) value);
                        else if (value instanceof Long) gen.writeNumber((Long) value);
                        else if (value instanceof Double) gen.writeNumber((Double) value);
                        else if (value instanceof Float) gen.writeNumber((Float) value);
                        else gen.writeNumber(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        gen.writeBoolean((Boolean) value);
                    } else {
                        gen.writeString(value.toString()); // geometries, dates, strings
                    }
                }
                gen.writeEndArray();
            }

            gen.writeEndArray(); // ]
            gen.flush();

            ByteBuffer resultBuffer = out.getBuffer();
            resultBuffer.flip();
            int len = resultBuffer.limit();

            long addr = unsafe.allocateMemory(len);
            for (int i = 0; i < len; i++) {
                unsafe.putByte(addr + i, resultBuffer.get(i));
            }

            if (sizeOutPtr.rawValue() != 0L) unsafe.putLong(sizeOutPtr.rawValue(), len);
            return WordFactory.pointer(addr);

        } catch (Exception e) {
            System.err.println("Error in h2gis_fetch_rows: " + e.getMessage());
            if (sizeOutPtr.rawValue() != 0L) unsafe.putLong(sizeOutPtr.rawValue(), 0L);
            return WordFactory.zero();
        }
    }

    @CEntryPoint(name = "h2gis_fetch_row")
    public static WordBase h2gisFetchRow(IsolateThread thread, long queryHandle, int index, WordBase sizeOutPtr) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {
                // Si le pointeur sizeOutPtr est valide, écrire 0 dedans
                if (sizeOutPtr.rawValue() != 0L) {
                    unsafe.putLong(sizeOutPtr.rawValue(), 0L);
                }
                return WordFactory.zero();
            }

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            if (colCount < 1) {
                if (sizeOutPtr.rawValue() != 0L) {
                    unsafe.putLong(sizeOutPtr.rawValue(), 0L);
                }
                return WordFactory.zero();
            }

            ByteArrayOutputStream valueBuffer = new ByteArrayOutputStream();
            int rowCount = 0;

            // On parcourt les résultats (attention : index de la colonne est fixé à 1 ici)
            while (rs.next()) {
                int colIndex = 1;
                Object val = rs.getObject(1); // tu peux changer la colonne si besoin
                int colType =  rs.getMetaData().getColumnType(colIndex);

                if (val == null) {
                    // Si null, écrire une valeur par défaut (ici 0 pour numérique, "" pour string)
                    // Ajuste selon ton type réel attendu
                    if (val instanceof Number) {
                        writeTypedValue(valueBuffer, 0, colType);
                    } else {
                        writeTypedValue(valueBuffer, "", colType);
                    }
                } else {

                    writeTypedValue(valueBuffer, val, colType);
                }
                rowCount++;
            }

            byte[] arr = valueBuffer.toByteArray();

            // Écrire le nombre de lignes dans la mémoire pointée par sizeOutPtr, si valide
            if (sizeOutPtr.rawValue() != 0L) {
                unsafe.putLong(sizeOutPtr.rawValue(), rowCount);
            }

            // Allouer la mémoire native pour le buffer
            long addr = unsafe.allocateMemory(arr.length);
            System.out.println("arr length : " + arr.length);
            System.out.println("arr length / 8 : " + (arr.length/8));
            System.out.println("rowcount : " + rowCount);


            // Copier les données dans la mémoire native allouée
            for (int i = 0; i < arr.length; i++) {
                unsafe.putByte(addr + i, arr[i]);
            }

            // Retourner un pointeur vers cette mémoire (WordBase)
            return WordFactory.pointer(addr);

        } catch (Exception e) {
            System.err.println("Error in h2gis_fetch_row: " + e.getMessage());

            // En cas d'erreur, écrire 0 dans sizeOutPtr si valide
            if (sizeOutPtr.rawValue() != 0L) {
                unsafe.putLong(sizeOutPtr.rawValue(), 0L);
            }
            return WordFactory.zero();
        }
    }

    @CEntryPoint(name = "h2gis_get_column_types")
    public static WordBase h2gisGetColumnTypes(IsolateThread thread, long queryHandle, WordBase colCountOut) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {
                // Si le pointeur sizeOutPtr est valide, écrire 0 dedans
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


            // Then: write each type code
            for (int i = 1; i <= colCount; i++) {
                String sqlTypeName = meta.getColumnTypeName(i);
                int typeCode = meta.getColumnType(i);

                switch (typeCode) {
                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        typeCode = 1; // INT
                        break;
                    case Types.BIGINT:
                        System.out.println("BIGINT detected");
                        typeCode = 2; // LONG (add this line)
                        break;
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        typeCode = 3; // FLOAT/DOUBLE
                        break;
                    case Types.BOOLEAN:
                    case Types.BIT:
                        typeCode = 4; // BOOL
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                        typeCode = 5; // STRING
                        break;
                    case Types.DATE:
                        typeCode = 6;
                        break;
                    default:
                        if(sqlTypeName.equals("GEOMETRY")) {
                            typeCode = 7;
                        }else{
                            typeCode = 99;
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


    private static void writeTypedValue(ByteArrayOutputStream out, Object val, int typeCode) throws IOException {
        ByteBuffer bb4 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer bb8 = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);

        switch (typeCode) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                int intVal = (val == null) ? 0 : ((Number) val).intValue();
                out.write(bb4.putInt(intVal).array());
                bb4.clear();
                break;

            case Types.BIGINT:
                long longVal = (val == null) ? 0L : ((Number) val).longValue();
                out.write(bb8.putLong(longVal).array());
                bb8.clear();
                break;

            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                double doubleVal = (val == null) ? 0.0 : ((Number) val).doubleValue();
                out.write(bb8.putDouble(doubleVal).array());
                bb8.clear();
                break;

            case Types.BOOLEAN:
            case Types.BIT:
                boolean boolVal = (val != null) && ((Boolean) val);
                out.write(boolVal ? 1 : 0);
                break;

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.DATE:
                // fallthrough
            default:
                String str = (val != null) ? val.toString() : "";
                byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
                out.write(bb4.putInt(utf8.length).array());
                bb4.clear();
                out.write(utf8);
                break;
        }
    }




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
}
