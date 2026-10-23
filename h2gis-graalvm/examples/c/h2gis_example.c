/*
 * H2GIS native library (C API version 2): a complete example.
 *
 * Opens an in-memory database, creates a spatial table, inserts rows with a
 * prepared statement and a batch (NULL values included), runs a spatial query
 * with a parameter, reads the result in batches, and reports errors.
 *
 * Build (h2gis.h, graal_isolate.h and h2gis.so are produced by native-image):
 *   gcc -I<lib dir> h2gis_example.c -o h2gis_example -L<lib dir> -l:h2gis.so
 *   LD_LIBRARY_PATH=<lib dir> ./h2gis_example
 *
 * This code is part of the H2GIS project, LGPL 3.0.
 */
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <graal_isolate.h>
#include <h2gis.h>

/* Type codes of the result buffer (org.h2gis.graalvm.TypeCodes) */
enum { T_INT = 1, T_LONG = 2, T_FLOAT = 3, T_DOUBLE = 4, T_BOOLEAN = 5, T_GEOMETRY = 8,
       T_BINARY = 9 };
#define NULL_MASK_FLAG 0x100

static graal_isolatethread_t *thread;

/* Print the last error of this thread and free it. */
static int fail(const char *what) {
    char *msg = h2gis_get_last_error(thread);
    fprintf(stderr, "%s failed: %s\n", what, msg);
    h2gis_free_string(thread, msg);
    return 1;
}

/* The buffer is little endian; read integers byte by byte to stay portable. */
static uint32_t u32(const unsigned char *p) {
    return p[0] | (p[1] << 8) | (p[2] << 16) | ((uint32_t) p[3] << 24);
}
static uint64_t u64(const unsigned char *p) {
    return u32(p) | ((uint64_t) u32(p + 4) << 32);
}

/* Print a result buffer as a table. Returns the number of rows. */
static uint32_t print_buffer(const unsigned char *buf, int header) {
    uint32_t ncols = u32(buf), nrows = u32(buf + 4);
    const unsigned char *col[64];
    const unsigned char *mask[64];
    const unsigned char *values[64];
    uint32_t type[64];
    for (uint32_t c = 0; c < ncols && c < 64; c++) {
        const unsigned char *p = buf + u64(buf + 8 + 8 * c);
        uint32_t name_len = u32(p);
        col[c] = p;                              /* name at p + 4 */
        p += 4 + name_len;
        uint32_t code = u32(p);
        p += 4;
        type[c] = code & ~NULL_MASK_FLAG;
        mask[c] = (code & NULL_MASK_FLAG) ? p : NULL;
        if (mask[c]) p += (nrows + 7) / 8;
        values[c] = p + 4;                       /* skip values_length */
        if (header) printf("%-14.*s", (int) name_len, (const char *) col[c] + 4);
    }
    if (header) printf("\n");
    for (uint32_t r = 0; r < nrows; r++) {
        for (uint32_t c = 0; c < ncols && c < 64; c++) {
            int is_null = mask[c] && (mask[c][r / 8] >> (r % 8) & 1);
            const unsigned char *v = values[c];
            switch (type[c]) {                   /* print, then advance to the next value */
                case T_INT:     if (is_null) printf("%-14s", "NULL"); else printf("%-14d", (int32_t) u32(v));
                                values[c] += 4; break;
                case T_LONG:    if (is_null) printf("%-14s", "NULL"); else printf("%-14lld", (long long) u64(v));
                                values[c] += 8; break;
                case T_FLOAT:   { float f; memcpy(&f, v, 4); if (is_null) printf("%-14s", "NULL"); else printf("%-14g", f); }
                                values[c] += 4; break;
                case T_DOUBLE:  { double d; memcpy(&d, v, 8); if (is_null) printf("%-14s", "NULL"); else printf("%-14g", d); }
                                values[c] += 8; break;
                case T_BOOLEAN: printf("%-14s", is_null ? "NULL" : (*v ? "true" : "false"));
                                values[c] += 1; break;
                default: {      /* length + bytes: text, or EWKB for geometries */
                    uint32_t len = u32(v);
                    if (is_null) printf("%-14s", "NULL");
                    else if (type[c] == T_GEOMETRY || type[c] == T_BINARY) printf("<%u bytes>     ", len);
                    else printf("%-14.*s", (int) len, (const char *) v + 4);
                    values[c] += 4 + len;
                }
            }
        }
        printf("\n");
    }
    return nrows;
}

int main(void) {
    graal_isolate_t *isolate = NULL;
    if (graal_create_isolate(NULL, &isolate, &thread) != 0) {
        fprintf(stderr, "cannot create the isolate\n");
        return 1;
    }
    printf("H2GIS C API version %d\n", h2gis_version(thread));

    /* "mem:demo" = in-memory database; a file database is "/path/to/db" */
    long long conn = h2gis_connect(thread, "mem:demo", "sa", "");
    if (!conn) return fail("connect");

    if (h2gis_execute(thread, conn,
            "CREATE TABLE PLACES(ID INT PRIMARY KEY, NAME VARCHAR, POP BIGINT, "
            "THE_GEOM GEOMETRY(POINT, 4326))") < 0) return fail("create table");

    /* Prepared statement + batch; the geometry is built in SQL from WKT */
    long long stmt = h2gis_prepare(thread, conn,
            "INSERT INTO PLACES VALUES (?, ?, ?, ST_GeomFromText(?, 4326))");
    if (!stmt) return fail("prepare");
    struct { int id; char *name; long long pop; char *wkt; } rows[] = {
        {1, "Paris", 2103778, "POINT(2.3522 48.8566)"},
        {2, "Lyon", 522250, "POINT(4.8357 45.7640)"},
        {3, "Nowhere", -1, NULL},               /* NULL population and geometry */
    };
    for (int i = 0; i < 3; i++) {
        if (h2gis_bind_int(thread, stmt, 1, rows[i].id) != 0
                || h2gis_bind_string(thread, stmt, 2, rows[i].name) != 0
                || (rows[i].pop < 0 ? h2gis_bind_null(thread, stmt, 3)
                                    : h2gis_bind_long(thread, stmt, 3, rows[i].pop)) != 0
                || h2gis_bind_string(thread, stmt, 4, rows[i].wkt) != 0   /* NULL pointer = SQL NULL */
                || h2gis_add_batch(thread, stmt) != 0) return fail("bind");
    }
    long long inserted = h2gis_execute_batch(thread, stmt);
    if (inserted < 0) return fail("batch");
    h2gis_close_query(thread, stmt);
    printf("%lld rows inserted\n\n", inserted);

    /* Spatial query with a parameter, read 2 rows at a time */
    stmt = h2gis_prepare(thread, conn,
            "SELECT ID, NAME, POP, ST_X(THE_GEOM) AS X, THE_GEOM FROM PLACES "
            "WHERE THE_GEOM IS NULL OR ST_Y(THE_GEOM) > ? ORDER BY ID");
    if (!stmt || h2gis_bind_double(thread, stmt, 1, 45.0) != 0) return fail("prepare query");
    long long rs = h2gis_execute_prepared(thread, stmt);
    if (!rs) return fail("query");
    long long size = 0;
    int first = 1;
    for (;;) {
        unsigned char *buf = h2gis_fetch_batch(thread, rs, 2, &size);
        if (!buf) return fail("fetch");
        uint32_t n = print_buffer(buf, first);
        h2gis_free_result_buffer(thread, buf);
        first = 0;
        if (n < 2) break;                        /* fewer rows than asked: the end */
    }
    h2gis_close_query(thread, stmt);             /* also closes its result set */

    /* Errors: the message ends with the H2 error code */
    if (h2gis_fetch(thread, conn, "SELECT * FROM NOPE") == 0) {
        char *msg = h2gis_get_last_error(thread);
        printf("\nexpected error: %s\n", msg);
        h2gis_free_string(thread, msg);
    }

    h2gis_close_connection(thread, conn);
    graal_tear_down_isolate(thread);
    return 0;
}
