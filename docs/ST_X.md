# ST_X

## Signature

```sql
DOUBLE ST_X(GEOMETRY geom);
```

## Description

Returns the x-value of the first coordinate of `geom`.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_X('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: 4.0
```

```sql
SELECT ST_X(
    ST_GeometryN('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))', 2));
-- Answer: 1.0
```

```sql
SELECT ST_X('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 2.0
```

```sql
SELECT ST_X(ST_PointN('LINESTRING(2 1, 1 3, 5 2)', 3));
-- Answer: 5.0

SELECT ST_X('POLYGON((5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: 5.0
```

```sql
SELECT ST_X(
    ST_PointN(
        ST_ExteriorRing('POLYGON((5 0, 7 0, 7 1, 5 1, 5 0))'), 3));
-- Answer: 7.0
```

```sql
SELECT ST_X('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)),
                          ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: 0.0
```

```sql
SELECT ST_X('GEOMETRYCOLLECTION(
               MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
               LINESTRING(2 1, 1 3, 5 2),
               POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 4.0

```

## See also

* [`ST_Y`](../ST_Y), [`ST_Z`](../ST_Z)
* [`ST_GeometryN`](../ST_GeometryN), [`ST_PointN`](../ST_PointN), [`ST_ExteriorRing`](../ST_ExteriorRing)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_X.java" target="_blank">Source code</a>
