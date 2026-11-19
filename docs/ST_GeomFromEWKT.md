# ST_GeomFromEWKT

## Signature

```sql
GEOMETRY ST_GeomFromEWKT(VARCHAR ewkt);
```

## Description

Converts the [Extended Well Known Text](https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry) `ewkt` into a Geometry.

The EWKT format is the WKT format prefixed by the spatial reference id of the geometry,
e.g. `SRID=4326;POINT(-71.161144 42.25932)`.
If the `SRID=...;` prefix is omitted, the spatial reference id of the returned geometry is 0.

Z and M coordinates are supported (e.g. `POINT Z`, `POINT M`, `POINT ZM`).

Returns `NULL` if `ewkt` is `NULL`. An error is raised if `ewkt` cannot be parsed.

## Examples

```sql
SELECT ST_GeomFromEWKT('SRID=4326;POINT(-71.161144 42.25932)');
-- Answer: POINT (-71.161144 42.25932)
```

```sql
SELECT ST_SRID(ST_GeomFromEWKT('SRID=4326;POINT(-71.161144 42.25932)'));
-- Answer: 4326
```

```sql
SELECT ST_SRID(ST_GeomFromEWKT('LINESTRING(0 0, 1 1)'));
-- Answer: 0
```

```sql
SELECT CAST(ST_GeomFromEWKT('SRID=2154;POINT ZM(1 2 3 4)') AS VARCHAR);
-- Answer: SRID=2154;POINT ZM (1 2 3 4)
```

```sql
SELECT ST_GeomFromEWKT('SRID=4326;POINT(1)');
-- Answer: Error: Cannot parse the EWKT format.
```

## See also

* [`ST_GeomFromText`](../ST_GeomFromText), [`ST_GeomFromWKB`](../ST_GeomFromWKB), [`ST_AsEWKB`](../ST_AsEWKB), [`ST_AsText`](../ST_AsText), [`ST_SRID`](../ST_SRID)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_GeomFromEWKT.java" target="_blank">Source code</a>
