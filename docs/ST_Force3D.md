# ST_Force3D

## Signature

```sql
GEOMETRY ST_Force3D(GEOMETRY geom);
GEOMETRY ST_Force3D(GEOMETRY geom, zValue DOUBLE);
```

## Description

Converts `geom` to a 3D Geometry by setting its nonexistent *z*-values to 0.

User can set a default `Z` value (`zValue`). If not specified, `zValue` is set to zero.

If `geom` has already a `M` value, then it is removed. 

Already 3D (`XYZ`) geometries are returned untouched.

## Examples

### No effect on `XYZ` geometries

```sql
SELECT ST_Force3D('POINT Z(-10 10 6)');
-- Answer:         POINT Z(-10 10 6)
```

### Adding `Z` dimension

```sql
SELECT ST_Force3D('POINT(-10 10)');
-- Answer:         POINT Z(-10 10 0)
```

```sql
SELECT ST_Force3D('LINESTRING(-10 10, 10 10)');
-- Answer:         LINESTRING Z(-10 10 0, 10 10)
```

```sql
SELECT ST_Force3D('POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))');
-- Answer:         POLYGON Z ((2 2 0, 10 0 0, 10 5 0, 0 5 0, 2 2 0))
```

```sql
SELECT ST_Force3D('GEOMETRYCOLLECTION(
                       POINT(1 1), 
                       LINESTRING(-10 10, 10 10), 
                       POLYGON((2 2, 10 0, 10 5, 0 5, 2 2)))');
-- Answer:         GEOMETRYCOLLECTION Z (
--                     POINT Z (1 1 0), 
--                     LINESTRING Z (-10 10 0, 10 10 0), 
--                     POLYGON Z ((2 2 0, 10 0 0, 10 5 0, 0 5 0, 2 2 0)))
```

### Specifying `zValue`

```sql
SELECT ST_Force3D('POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))', 5);
-- Answer:         POLYGON Z((2 2 5, 10 0 5, 10 5 5, 0 5 5, 2 2 5))
```

```sql
SELECT ST_Force3D('MULTIPOINT((2 2), (10 0))', 5);
-- Answer:         MULTIPOINT Z ((2 2 5), (10 0 5))
```

### Removing `M` dimension and adding the `Z` one

```sql
SELECT ST_Force3D('POINT M(-10 10 12)',10);
-- Answer:         POINT Z(-10 10 10)

SELECT ST_Force3D('LINESTRING M(-10 10 4, 10 10 6)', 3);
-- Answer:         LINESTRING Z(-10 10 3, 10 10 3)

SELECT ST_Force3D('POLYGON M((2 2 4, 10 0 3, 10 5 2, 0 5 1, 2 2 4))', 5);
-- Answer:         POLYGON Z((2 2 5, 10 0 5, 10 5 5, 0 5 5, 2 2 5))
```

## See also

* [`ST_Force2D`](../ST_Force2D), [`ST_Force3DM`](../ST_Force3DM), [`ST_Force4D`](../ST_Force4D), [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_Force3D.java" target="_blank">Source code</a>
