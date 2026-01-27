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
 * <p>
 * This class manages connections, statements, result sets, executes queries,
 * and returns results as raw buffers to native code.
 *
 * @author Maël PHILIPPE, CNRS
 * @author Erwan BOCHER, CNRS
 */
public class GraalCInterface {


    /**
     * Logger for internal error reporting
     */
    private static final Logger LOGGER = Logger.getLogger(GraalCInterface.class.getName());

    /**
     * Maps handle -> JDBC connection
     */
    private static final Map<Long, Connection> connections = new ConcurrentHashMap<>();

    /**
     * Maps handle -> JDBC statement
     */
    private static final Map<Long, Statement> statements = new ConcurrentHashMap<>();

    /**
     * Maps handle -> JDBC result set
     */
    private static final Map<Long, ResultSet> results = new ConcurrentHashMap<>();

    /**
     * Handle counter to assign unique IDs to each resource
     */
    private static final AtomicLong handleCounter = new AtomicLong(1);

    /**
     * Thread-local error message, retrieved by C with h2gis_get_last_error()
     */
    private static final ThreadLocal<String> lastError = new ThreadLocal<>();

    /**
     * Unsafe is used for direct memory allocation/free to pass buffers to native side
     */
    private static final Unsafe unsafe = getUnsafe();

    /**
     * Lock for connection synchronization
     */
    private static final Object connectLock = new Object();


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
     *
     * @param thread the current Graal Isolate thread
     * @return C string pointer to the last error message or empty string if none
     */
    @CEntryPoint(name = "h2gis_get_last_error")
    public static CCharPointer h2gisGetLastError(IsolateThread thread) {
        String error = lastError.get();
        lastError.remove();
        if (error == null) {
            error = "";
        }
        return toCString(error).get();
    }

    /**
     * Opens a new connection to an H2GIS database.
     *
     * @param thread          the current Graal Isolate thread
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

            Connection connection;
            synchronized (connectLock) {
                connection = JDBCUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(properties)).getConnection();
            }

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
     *
     * @param thread           the current Graal Isolate thread
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
     *
     * @param thread           the current Graal Isolate thread
     * @param connectionHandle handle representing an active connection
     * @param queryPointer     C pointer to the SQL SELECT query string
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
     *
     * @param thread           the current Graal Isolate thread
     * @param connectionHandle handle representing an active connection
     * @param queryPointer     C pointer to the SQL update query string
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
     * Prepares a SQL statement.
     */
    @CEntryPoint(name = "h2gis_prepare")
    public static long h2gisPrepare(IsolateThread thread, long connectionHandle, CCharPointer sqlPointer) {
        if (sqlPointer.isNull()) {
            logAndSetError("Null pointer for SQL", null);
            return 0;
        }
        Connection conn = connections.get(connectionHandle);
        if (conn == null) return 0;
        
        try {
            String sql = CTypeConversion.toJavaString(sqlPointer);
            PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            long handle = handleCounter.getAndIncrement();
            statements.put(handle, pstmt);
            return handle;
        } catch(Exception e) {
            logAndSetError("Prepare failed", e);
            return 0;
        }
    }

    @CEntryPoint(name = "h2gis_bind_double")
    public static void h2gisBindDouble(IsolateThread thread, long stmtHandle, int index, double value) {
         Statement s = statements.get(stmtHandle);
         if (s instanceof PreparedStatement) {
             try {
                 ((PreparedStatement)s).setDouble(index, value);
             } catch(SQLException e) {
                 logAndSetError("Bind double failed", e);
             }
         }
    }

    @CEntryPoint(name = "h2gis_bind_int")
    public static void h2gisBindInt(IsolateThread thread, long stmtHandle, int index, int value) {
         Statement s = statements.get(stmtHandle);
         if (s instanceof PreparedStatement) {
             try {
                 ((PreparedStatement)s).setInt(index, value);
             } catch(SQLException e) {
                 logAndSetError("Bind int failed", e);
             }
         }
    }

    @CEntryPoint(name = "h2gis_bind_long")
    public static void h2gisBindLong(IsolateThread thread, long stmtHandle, int index, long value) {
         Statement s = statements.get(stmtHandle);
         if (s instanceof PreparedStatement) {
             try {
                 ((PreparedStatement)s).setLong(index, value);
             } catch(SQLException e) {
                 logAndSetError("Bind long failed", e);
             }
         }
    }

    @CEntryPoint(name = "h2gis_bind_string")
    public static void h2gisBindString(IsolateThread thread, long stmtHandle, int index, CCharPointer value) {
         Statement s = statements.get(stmtHandle);
         if (s instanceof PreparedStatement) {
             try {
                 String str = CTypeConversion.toJavaString(value);
                 ((PreparedStatement)s).setString(index, str);
             } catch(SQLException e) {
                 logAndSetError("Bind string failed", e);
             }
         }
    }

    @CEntryPoint(name = "h2gis_bind_blob")
    public static void h2gisBindBlob(IsolateThread thread, long stmtHandle, int index, CCharPointer value, int len) {
         Statement s = statements.get(stmtHandle);
         if (s instanceof PreparedStatement) {
             try {
                 // Copy native bytes to Java byte array
                 byte[] bytes = new byte[len];
                 for(int i=0; i<len; i++) {
                     bytes[i] = value.read(i);
                 }
                 ((PreparedStatement)s).setBytes(index, bytes);
             } catch(SQLException e) {
                 logAndSetError("Bind blob failed", e);
             }
         }
    }

    @CEntryPoint(name = "h2gis_execute_prepared_update")
    public static int h2gisExecutePreparedUpdate(IsolateThread thread, long stmtHandle) {
         Statement s = statements.get(stmtHandle);
         if (s instanceof PreparedStatement) {
             try {
                 return ((PreparedStatement)s).executeUpdate();
             } catch(SQLException e) {
                 logAndSetError("Execute prepared update failed", e);
                 return -1;
             }
         }
         return -1;
    }

    @CEntryPoint(name = "h2gis_execute_prepared")
    public static long h2gisExecutePrepared(IsolateThread thread, long stmtHandle) {
         Statement s = statements.get(stmtHandle);
         if (s instanceof PreparedStatement) {
             try {
                 ResultSet rs = ((PreparedStatement)s).executeQuery();
                 long rsHandle = handleCounter.getAndIncrement();
                 // We do NOT put the statement in 'statements' again, it is already there.
                 // We just track the Result Set.
                 // WARNING: Our cleanup logic might need to know which statement owns which RS? 
                 // For now, simple mapping.
                 results.put(rsHandle, rs);
                 return rsHandle;
             } catch(SQLException e) {
                 logAndSetError("Execute prepared failed", e);
                 return 0;
             }
         }
         return 0;
    }

    /**
     * Closes a previously opened query result set and associated statement.
     *
     * @param thread      the current Graal Isolate thread
     * @param queryHandle handle representing the query to close
     */
    @CEntryPoint(name = "h2gis_close_query")
    public static void h2gisCloseQuery(IsolateThread thread, long queryHandle) {
        ResultSet rs = results.remove(queryHandle);
        Statement stmt = statements.remove(queryHandle);
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            logAndSetError("Failed to close ResultSet", e);
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (Exception e) {
            logAndSetError("Failed to close Statement", e);
        }
    }


    /**
     * Closes a previously opened connection.
     *
     * @param thread           the current Graal Isolate thread
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
     *
     * @param thread           the current Graal Isolate thread
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
                } else {
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
     *
     * @param thread      the current Graal Isolate thread
     * @param queryHandle handle representing the query result set
     * @param bufferSize  pointer to store the size of the returned memory buffer
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
     *
     * @param thread      the current Graal Isolate thread
     * @param queryHandle handle representing the query result set
     * @param bufferSize  pointer to store the size of the returned memory buffer
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
     * Fetches a batch of rows from the result set of a query and returns them
     * in an encoded native memory buffer.
     *
     * @param thread      the current Graal Isolate thread
     * @param queryHandle handle representing the query result set
     * @param batchSize   the maximum number of rows to fetch
     * @param bufferSize  pointer to store the size of the returned memory buffer
     * @return native memory pointer to the JSON buffer, or 0 on error
     */
    @CEntryPoint(name = "h2gis_fetch_batch")
    public static WordBase h2gisFetchBatch(IsolateThread thread, long queryHandle, int batchSize, WordBase bufferSize) {
        try {
            ResultSet rs = results.get(queryHandle);
            if (rs == null) {

                if (bufferSize.rawValue() != 0L) {
                    unsafe.putLong(bufferSize.rawValue(), 0L);
                }
                return WordFactory.zero();
            }
            ResultSetWrapper resultSetWrapper = ResultSetWrapper.fromBatch(rs, batchSize);

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

            // If no more rows, close the result set
            // Wait, standard practice in iterators: only close if explicitly asked or if we know we are done.
            // But we don't know if we are done just because we fetched < batchSize (maybe last batch was exactly batchSize).
            // Safer to check rs.isAfterLast() or let the user close it.
            // Checking: resultSetWrapper.getRowCount() < batchSize implies end of stream.
            // But let's leave explicit close to h2gis_close_query.

            return WordFactory.pointer(addr);

        } catch (Exception e) {
            System.err.println("Error in h2gis_fetch_batch: " + e.getMessage());
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
     *
     * @param thread      the current Graal Isolate thread
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

    @CEntryPoint(name = "h2gis_get_metadata_json")
    public static CCharPointer h2gisGetMetadataJson(IsolateThread thread, long queryHandle) {
         try {
             ResultSet rs = results.get(queryHandle);
             if (rs == null) return CTypeConversion.toCString("[]").get();
             
             ResultSetMetaData meta = rs.getMetaData();
             int colCount = meta.getColumnCount();
             
             // Build JSON: [{"name":"COL1","type":1,"typeName":"INTEGER"}, ...]
             StringBuilder json = new StringBuilder("[");
             for(int i=1; i<=colCount; i++) {
                 if(i > 1) json.append(",");
                 json.append("{");
                 json.append("\"name\":\"").append(meta.getColumnName(i)).append("\",");
                 json.append("\"typeName\":\"").append(meta.getColumnTypeName(i)).append("\",");
                 
                 int jdbcType = meta.getColumnType(i);
                 int typeCode = 99; // Default
                 String sqlTypeName = meta.getColumnTypeName(i).toLowerCase();
                 
                 // Same switch case as above (simplified for brevity here, should factor out)
                 switch (jdbcType) {
                    case Types.INTEGER: case Types.SMALLINT: case Types.TINYINT: typeCode = 1; break;
                    case Types.BIGINT: typeCode = 2; break;
                    case Types.FLOAT: case Types.REAL: typeCode = 3; break;
                    case Types.DOUBLE: case Types.NUMERIC: case Types.DECIMAL: typeCode = 4; break;
                    case Types.BOOLEAN: case Types.BIT: typeCode = 5; break;
                    case Types.CHAR: case Types.VARCHAR: case Types.LONGVARCHAR: typeCode = 6; break;
                    case Types.DATE: case Types.TIME: case Types.TIMESTAMP: typeCode = 7; break;
                    default:
                        if (sqlTypeName.startsWith("geometry")) typeCode = 8;
                        else typeCode = 99;
                        break;
                 }
                 json.append("\"type\":").append(typeCode);
                 json.append("}");
             }
             json.append("]");
             
             return CTypeConversion.toCString(json.toString()).get();
             
         } catch(Exception e) {
             logAndSetError("Failed to get metadata json", e);
             return CTypeConversion.toCString("[]").get();
         }
    }


    /**
     * Frees a previously stored query result set, closing its resources.
     *
     * @param thread      the current Graal Isolate thread
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
     *
     * @param thread the current Graal Isolate thread
     * @param ptr    native pointer to the buffer to free
     */
    @CEntryPoint(name = "h2gis_free_result_buffer")
    public static void freeResultBuffer(IsolateThread thread, WordBase ptr) {
        if (ptr.rawValue() != 0L) {
            unsafe.freeMemory(ptr.rawValue());
        }
    }


    /**
     * Logs an error and sets it in the thread-local error variable.
     *
     * @param message the error message to log
     * @param e       the exception that was thrown, or null
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
     *
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
