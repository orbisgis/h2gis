# Examples of the H2GIS native library

Both examples do the same session with the C API version 2
([C_API.md](../C_API.md)): open an in-memory database, create a spatial
table, insert rows with a prepared statement and a batch (NULL values
included), run a spatial query with a parameter, read the result in batches
of 2 rows, decode the result buffer (null masks included) and report an error.

The native build (`mvn native:compile -Pnative -pl h2gis-graalvm`) writes
`h2gis.so` (`h2gis.dll`, `h2gis.dylib`), `h2gis.h` and `graal_isolate.h` to
`h2gis-graalvm/target`.

## C

```bash
LIB=h2gis-graalvm/target
gcc -I$LIB examples/c/h2gis_example.c -o h2gis_example -L$LIB -l:h2gis.so
LD_LIBRARY_PATH=$LIB ./h2gis_example
```

```
H2GIS C API version 2
3 rows inserted

ID            NAME          POP           X             THE_GEOM
1             Paris         2103778       2.3522        <25 bytes>
2             Lyon          522250        4.8357        <25 bytes>
3             Nowhere       NULL          NULL          NULL

expected error: Failed to execute query: Table "NOPE" not found; SQL statement:
SELECT * FROM NOPE [42102-240]
```

## Python (ctypes, no dependency)

```bash
python examples/python/h2gis_ctypes_example.py h2gis-graalvm/target/h2gis.so
```

For applications, use [h2gis-python](https://github.com/orbisgis/h2gis-python),
a DB-API 2.0 driver (psycopg 3 style API, Shapely geometries, GeoPandas) built
on this library:

```python
import h2gis

with h2gis.connect("mem:demo", lib_path="h2gis-graalvm/target/h2gis.so") as conn:
    conn.execute("CREATE TABLE PLACES(ID INT, NAME VARCHAR, THE_GEOM GEOMETRY(POINT, 4326))")
    conn.execute("INSERT INTO PLACES VALUES (%s, %s, ST_GeomFromText(%s, 4326))",
                 [1, "Paris", "POINT(2.3522 48.8566)"])
    print(conn.execute("SELECT * FROM PLACES").fetchall())
    gdf = h2gis.read_gdf(conn, "SELECT * FROM PLACES")
```
