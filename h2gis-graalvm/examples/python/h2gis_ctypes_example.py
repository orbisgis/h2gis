"""
H2GIS native library (C API version 2) used directly from Python with ctypes,
without any dependency.

For real work use h2gis-python (https://github.com/orbisgis/h2gis-python), a
DB-API 2.0 driver built on this library. This file shows what it does under
the hood: isolate, connection, prepared statements, batches, result buffer
decoding (null masks included), errors and memory.

    python h2gis_ctypes_example.py /path/to/h2gis.so

This code is part of the H2GIS project, LGPL 3.0.
"""

import ctypes
import struct
import sys

lib = ctypes.CDLL(sys.argv[1] if len(sys.argv) > 1 else "./h2gis.so")

# --- declarations -------------------------------------------------------------
thread_p = ctypes.c_void_p
jlong, jint = ctypes.c_int64, ctypes.c_int32   # Java long and int, on every platform
SIGNATURES = {
    "h2gis_version": ([thread_p], jint),
    "h2gis_get_last_error": ([thread_p], ctypes.c_void_p),     # string to free
    "h2gis_free_string": ([thread_p, ctypes.c_void_p], None),
    "h2gis_free_result_buffer": ([thread_p, ctypes.c_void_p], None),
    "h2gis_connect": ([thread_p, ctypes.c_char_p, ctypes.c_char_p, ctypes.c_char_p], jlong),
    "h2gis_close_connection": ([thread_p, jlong], None),
    "h2gis_execute": ([thread_p, jlong, ctypes.c_char_p], jint),
    "h2gis_fetch": ([thread_p, jlong, ctypes.c_char_p], jlong),
    "h2gis_prepare": ([thread_p, jlong, ctypes.c_char_p], jlong),
    "h2gis_bind_long": ([thread_p, jlong, jint, jlong], jint),
    "h2gis_bind_double": ([thread_p, jlong, jint, ctypes.c_double], jint),
    "h2gis_bind_string": ([thread_p, jlong, jint, ctypes.c_char_p], jint),
    "h2gis_bind_null": ([thread_p, jlong, jint], jint),
    "h2gis_add_batch": ([thread_p, jlong], jint),
    "h2gis_execute_batch": ([thread_p, jlong], jlong),
    "h2gis_execute_prepared": ([thread_p, jlong], jlong),
    "h2gis_close_query": ([thread_p, jlong], None),
    "h2gis_fetch_all": ([thread_p, jlong, ctypes.c_void_p], ctypes.c_void_p),
    "h2gis_fetch_batch": ([thread_p, jlong, jint, ctypes.c_void_p], ctypes.c_void_p),
    "h2gis_get_metadata_json": ([thread_p, jlong], ctypes.c_void_p),
}
for name, (args, res) in SIGNATURES.items():
    fn = getattr(lib, name)
    fn.argtypes, fn.restype = args, res

# --- isolate --------------------------------------------------------------------
isolate, thread = ctypes.c_void_p(), ctypes.c_void_p()
assert lib.graal_create_isolate(None, ctypes.byref(isolate), ctypes.byref(thread)) == 0
# One isolate thread per OS thread: another Python thread must call
# graal_attach_thread(isolate, &its_thread), and graal_detach_thread() at the end.


def take_string(ptr):
    """Read a string returned by the library, then free it."""
    try:
        return ctypes.string_at(ptr).decode("utf-8") if ptr else ""
    finally:
        lib.h2gis_free_string(thread, ptr)


def check(ok, what):
    if not ok:
        raise RuntimeError(f"{what}: {take_string(lib.h2gis_get_last_error(thread))}")


# --- result buffer --------------------------------------------------------------
FIXED = {1: "<i", 2: "<q", 3: "<f", 4: "<d", 5: "<?"}   # INT LONG FLOAT DOUBLE BOOLEAN
NULL_MASK_FLAG = 0x100


def decode(buf):
    """Decode a result buffer into (column names, type codes, rows)."""
    ncols, nrows = struct.unpack_from("<II", buf, 0)
    names, codes, columns = [], [], []
    for c in range(ncols):
        pos = struct.unpack_from("<Q", buf, 8 + 8 * c)[0]
        name_len = struct.unpack_from("<I", buf, pos)[0]
        names.append(buf[pos + 4:pos + 4 + name_len].decode("utf-8"))
        pos += 4 + name_len
        code = struct.unpack_from("<I", buf, pos)[0]
        pos += 4
        mask = None
        if code & NULL_MASK_FLAG:
            mask = buf[pos:pos + (nrows + 7) // 8]
            pos += len(mask)
        code &= ~NULL_MASK_FLAG
        pos += 4                                         # values_length
        values = []
        for r in range(nrows):
            if code in FIXED:
                fmt = FIXED[code]
                value = struct.unpack_from(fmt, buf, pos)[0]
                pos += struct.calcsize(fmt)
            else:                                        # length + bytes
                length = struct.unpack_from("<I", buf, pos)[0]
                raw = buf[pos + 4:pos + 4 + length]
                pos += 4 + length
                # 8 GEOMETRY (EWKB) and 9 BINARY stay bytes, the others are text
                value = raw if code in (8, 9) else raw.decode("utf-8")
            is_null = mask is not None and mask[r // 8] >> (r % 8) & 1
            values.append(None if is_null else value)
        codes.append(code)
        columns.append(values)
    return names, codes, list(zip(*columns)) if columns else []


def fetch(rs, batch=None):
    """Read a result set: all at once, or ``batch`` rows per native call."""
    size = ctypes.c_int64(0)
    rows, names = [], None
    while True:
        if batch:
            ptr = lib.h2gis_fetch_batch(thread, rs, batch, ctypes.byref(size))
        else:
            ptr = lib.h2gis_fetch_all(thread, rs, ctypes.byref(size))
        check(ptr, "fetch")
        try:
            names, _, chunk = decode(ctypes.string_at(ptr, size.value))
        finally:
            lib.h2gis_free_result_buffer(thread, ptr)
        rows += chunk
        if not batch or len(chunk) < batch:             # fewer rows than asked: the end
            return names, rows


# --- a session --------------------------------------------------------------------
print("H2GIS C API version", lib.h2gis_version(thread))
conn = lib.h2gis_connect(thread, b"mem:demo", b"sa", b"")   # or b"/path/to/db"
check(conn, "connect")

check(lib.h2gis_execute(thread, conn, b"CREATE TABLE PLACES(ID INT PRIMARY KEY, NAME VARCHAR, "
                        b"POP BIGINT, THE_GEOM GEOMETRY(POINT, 4326))") >= 0, "create")

# Batch insert with a prepared statement; NULL population and geometry
stmt = lib.h2gis_prepare(thread, conn, b"INSERT INTO PLACES VALUES (?, ?, ?, ST_GeomFromText(?, 4326))")
check(stmt, "prepare")
for id_, name, pop, wkt in [(1, "Paris", 2103778, "POINT(2.3522 48.8566)"),
                            (2, "Lyon", 522250, "POINT(4.8357 45.7640)"),
                            (3, "Nowhere", None, None)]:
    check(lib.h2gis_bind_long(thread, stmt, 1, id_) == 0, "bind")
    check(lib.h2gis_bind_string(thread, stmt, 2, name.encode()) == 0, "bind")
    rc = lib.h2gis_bind_null(thread, stmt, 3) if pop is None else lib.h2gis_bind_long(thread, stmt, 3, pop)
    check(rc == 0, "bind")
    check(lib.h2gis_bind_string(thread, stmt, 4, wkt.encode() if wkt else None) == 0, "bind")
    check(lib.h2gis_add_batch(thread, stmt) == 0, "add batch")
inserted = lib.h2gis_execute_batch(thread, stmt)
check(inserted >= 0, "batch")
lib.h2gis_close_query(thread, stmt)
print(inserted, "rows inserted")

# Query with a parameter, read 2 rows per call
stmt = lib.h2gis_prepare(thread, conn, b"SELECT ID, NAME, POP, THE_GEOM FROM PLACES "
                                       b"WHERE THE_GEOM IS NULL OR ST_Y(THE_GEOM) > ? ORDER BY ID")
check(stmt and lib.h2gis_bind_double(thread, stmt, 1, 45.0) == 0, "prepare")
rs = lib.h2gis_execute_prepared(thread, stmt)
check(rs, "query")
print("metadata:", take_string(lib.h2gis_get_metadata_json(thread, rs)))
names, rows = fetch(rs, batch=2)
lib.h2gis_close_query(thread, stmt)              # also closes its result set
print(names)
for row in rows:
    print(row[:3], "EWKB:", row[3].hex() if row[3] else None)

# Errors end with the H2 error code: here 42102, table not found
if not lib.h2gis_fetch(thread, conn, b"SELECT * FROM NOPE"):
    print("expected error:", take_string(lib.h2gis_get_last_error(thread)).splitlines()[0])

lib.h2gis_close_connection(thread, conn)
