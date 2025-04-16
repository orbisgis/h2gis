# ST_Force3D

## Signature

```sql
GEOMETRY ST_Force3D(GEOMETRY geom);
```

## Description

Converts `geom` to a 3D Geometry by setting its nonexistent *z*-values to 0.
Entirely 3D Geometries are returned untouched.

## Examples

```sql
-- No effect on 3D Geometries:
SELECT ST_Force3D('POINT(-10 10 6)');
-- Answer:         POINT(-10 10 6)
```

```sql
SELECT ST_Force3D('POINT(-10 10)');
-- Answer:         POINT(-10 10 0)
```

```sql
SELECT ST_Force3D('LINESTRING(-10 10, 10 10 3)');
-- Answer:         LINESTRING(-10 10 0, 10 10 3)
```

```sql
SELECT ST_Force3D('POLYGON((2 2, 10 0 1, 10 5 1, 0 5 2, 2 2))');
-- Answer:         POLYGON((2 2 0, 10 0 1, 10 5 1, 0 5 2, 2 2 0))
```

## See also

* [`ST_Force2D`](../ST_Force2D), [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_Force3D.java" target="_blank">Source code</a>
