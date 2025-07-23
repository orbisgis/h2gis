# ST_AddZ

## Signature

```sql
GEOMETRY ST_AddZ(GEOMETRY geom, DOUBLE zToAdd);
```

## Description

Returns Geometry whose *z*-coordinates are the sum of `zToAdd` and
the corresponding *z*-coordinate of `geom`. Coordinates with no
*z*-coordinate are not updated.

## Examples

```sql
SELECT ST_AddZ('MULTIPOINT((190 300 1), (10 11))', 10);
-- Answer: MULTIPOINT((190 300 11), (10 11))
```

The second point has no z-value, so it is not updated.
```sql
SELECT ST_Z(ST_GeometryN(
                ST_AddZ('MULTIPOINT((190 300 1), (10 11))', 10),
                2));
-- Answer: NaN
```

```sql
SELECT ST_AddZ('MULTIPOINT((190 300 10), (10 11 5))', -10)
-- Answer: MULTIPOINT((190 300 0), (10 11 -5))
```

```sql
SELECT ST_AddZ('POLYGON((1 1 5, 1 7 10, 7 7 -1, 7 1 -1, 1 1 5))',
               -10);
-- Answer: POLYGON((1 1 -5, 1 7 0, 7 7 -11, 7 1 -11, 1 1 -5))
```

## See also

* [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_AddZ.java" target="_blank">Source code</a>
