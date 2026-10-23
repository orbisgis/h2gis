# H2GIS native library: C API

The `h2gis-graalvm` module builds H2GIS as a shared library (`h2gis.so`,
`h2gis.dll`, `h2gis.dylib`) with GraalVM Native Image Community Edition (Mandrel). This page
describes the C interface, version **2** (`h2gis_version()`).

The reference client with Python is [h2gis-python](https://github.com/orbisgis/h2gis-python).
Some examples in C and in Python (ctypes) are in available in [examples](examples/).

## Conventions

- Every function takes the GraalVM isolate thread as first argument
  (`graal_isolatethread_t *`), obtained from `graal_create_isolate()` or
  `graal_attach_thread()`. A thread must use its own isolate thread, and
  call `graal_detach_thread()` before it ends.
- Connections, statements and result sets are `long long` (64 bits) handles;
  `0` means an error.
- Functions returning a count return `-1` on error; bind functions return `0`
  on success and `-1` on error.
- After an error, `h2gis_get_last_error()` returns the message of the calling
  thread. JDBC errors end with the H2 error code, e.g.
  `Table "NOPE" not found; SQL statement: SELECT * FROM NOPE [42102-240]`.
- Buffers and strings returned by the library are allocated with
  `UnmanagedMemory.malloc` and must be released with
  `h2gis_free_result_buffer()` (or `h2gis_free_string()`).
- Strings passed to the library are NUL terminated UTF-8.
- Sizes are written as 64 bits integers to the `size_out` pointers.

## Functions

### Library

| Function | Returns |
|---|---|
| `int h2gis_version(thread)` | C API version (2) |
| `char *h2gis_get_last_error(thread)` | last error of the thread, `""` if none; cleared once read; free it |
| `void h2gis_free_result_buffer(thread, void *ptr)` | frees a buffer or string returned by the library |
| `void h2gis_free_string(thread, char *ptr)` | alias of `h2gis_free_result_buffer` |

### Connections

| Function | Returns |
|---|---|
| `long long h2gis_connect(thread, const char *path, const char *user, const char *password)` | connection handle; `path` is appended to `jdbc:h2:` (`/data/db`, `mem:test`, options after `;`). The H2GIS functions are loaded if missing, in-memory databases included |
| `long long h2gis_load(thread, long long conn)` | 1 after loading the H2GIS functions again, 0 on error |
| `void h2gis_close_connection(thread, long long conn)` | closes the connection and its statements and result sets |
| `void h2gis_delete_database_and_close(thread, long long conn)` | `DROP ALL OBJECTS DELETE FILES`, then closes |

### Plain statements

| Function | Returns |
|---|---|
| `int h2gis_execute(thread, long long conn, const char *sql)` | update count (several `;` separated statements are allowed) |
| `long long h2gis_fetch(thread, long long conn, const char *sql)` | result set handle of a query |

### Prepared statements

| Function | Returns |
|---|---|
| `long long h2gis_prepare(thread, long long conn, const char *sql)` | statement handle; parameters are JDBC `?` |
| `int h2gis_bind_int(thread, long long stmt, int index, int value)` | status (index is 1-based) |
| `int h2gis_bind_long(thread, long long stmt, int index, long long value)` | status |
| `int h2gis_bind_double(thread, long long stmt, int index, double value)` | status |
| `int h2gis_bind_string(thread, long long stmt, int index, const char *value)` | status; a NULL pointer binds SQL NULL |
| `int h2gis_bind_blob(thread, long long stmt, int index, const char *data, int length)` | status; EWKB is accepted for GEOMETRY parameters |
| `int h2gis_bind_null(thread, long long stmt, int index)` | status |
| `long long h2gis_execute_prepared(thread, long long stmt)` | result set handle; a new execution closes the previous result set of the statement |
| `int h2gis_execute_prepared_update(thread, long long stmt)` | update count |
| `int h2gis_add_batch(thread, long long stmt)` | status; adds the bound parameters to the batch |
| `long long h2gis_execute_batch(thread, long long stmt)` | total update count of the batch |

### Results

| Function | Returns |
|---|---|
| `void *h2gis_fetch_all(thread, long long rs, long long *size_out)` | buffer with all the remaining rows |
| `void *h2gis_fetch_batch(thread, long long rs, int n, long long *size_out)` | buffer with up to `n` rows; fewer rows means the end |
| `void *h2gis_fetch_one(thread, long long rs, long long *size_out)` | buffer with the next row (0 rows at the end) |
| `char *h2gis_get_metadata_json(thread, long long rs)` | `[{"name":..., "typeName":..., "type":...}]`; free it |
| `int *h2gis_get_column_types(thread, long long rs, long long *count_out)` | array of type codes; free it |
| `void h2gis_close_query(thread, long long handle)` | closes a result set or a statement (and the result sets of a prepared statement) |
| `long long h2gis_free_result_set(thread, long long rs)` | closes a result set but keeps the handle (prefer `h2gis_close_query`) |

## Result buffer

Little endian, column oriented:

```
u32 column_count
u32 row_count
u64 column_offset[column_count]        // from the start of the buffer
column:
    u32 name_length, utf-8 name        // the column label
    u32 type_code                      // | 0x100 when a null mask follows
    [u8 null_mask[(row_count + 7) / 8]] // bit (i % 8) of byte (i / 8) set: row i is NULL
    u32 values_length
    values                             // row_count values
```

A NULL keeps a placeholder value (0, false, empty) so that fixed size
columns stay aligned; only the mask tells it is NULL.

| Code | Type | Value encoding | SQL types |
|---|---|---|---|
| 1 | INT | int32 | TINYINT, SMALLINT, INTEGER |
| 2 | LONG | int64 | BIGINT |
| 3 | FLOAT | float32 | REAL |
| 4 | DOUBLE | float64 | DOUBLE PRECISION, FLOAT |
| 5 | BOOLEAN | uint8 | BOOLEAN |
| 6 | STRING | u32 length + UTF-8 | CHARACTER [VARYING], CLOB, ENUM |
| 7 | TEMPORAL | u32 length + ISO 8601 | DATE (`2024-01-02`), TIME (`10:11:12.5`), TIMESTAMP (`2024-01-02T10:11:12.123456789`), WITH TIME ZONE (`...+02:00`) |
| 8 | GEOMETRY | u32 length + EWKB | GEOMETRY (the SRID is in the EWKB) |
| 9 | BINARY | u32 length + bytes | BINARY [VARYING], BLOB |
| 10 | DECIMAL | u32 length + `BigDecimal.toPlainString()` | NUMERIC, DECIMAL, DECFLOAT (`NaN`, `Infinity` as is) |
| 11 | JSON | u32 length + UTF-8 JSON text | JSON |
| 12 | UUID | u32 length + text | UUID |
| 99 | OTHER | u32 length + `ResultSet.getString()` | ARRAY, INTERVAL, ROW... |

The mapping lives in `org.h2gis.graalvm.TypeCodes`.
