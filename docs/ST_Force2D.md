# ST_Force2D

## Signature

```sql
GEOMETRY ST_Force2D(GEOMETRY geom);
```

## Description

Converts a 3D Geometry to a 2D Geometry by deleting the *z*-value of
each coordinate if it exists.

## Examples

No effect on 2D Geometries:
```sql
SELECT ST_Force2D('POINT(-10 10)');
-- Answer:         POINT(-10 10)
```
```sql
SELECT ST_Force2D('POINT(-10 10 6)');
-- Answer:         POINT(-10 10)
```
```sql
SELECT ST_Force2D('LINESTRING(-10 10 2, 10 10 3)');
-- Answer:         LINESTRING(-10 10, 10 10)
```
```sql
SELECT ST_Force2D('POLYGON((2 2 2, 10 0 1, 10 5 1, 0 5 2, 2 2 2))');
-- Answer:         POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))
```

Also works on Geometries of mixed dimension:
```sql
SELECT ST_Force2D('LINESTRING(-10 10, 10 10 3)');
-- Answer:         LINESTRING(-10 10, 10 10)
```

## See also

* [`ST_Force3D`](../ST_Force3D), [`ST_Force3DM`](../ST_Force3DM), [`ST_Force4D`](../ST_Force4D), [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_Force2D.java" target="_blank">Source code</a>
