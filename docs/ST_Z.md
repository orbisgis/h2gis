# ST_Z

## Signature

```sql
DOUBLE ST_Z(GEOMETRY geom);
```

## Description

Returns the z-value of the first coordinate of `geom`.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_Z('LINESTRING(2 1 0, 1 3 3, 5 2 1)');
-- Answer: 0.0

SELECT ST_Z('POLYGON((5 0 2, 7 0 4, 7 1 3, 5 1 6, 5 0 1))');
-- Answer: 2.0

SELECT ST_Z(
    ST_PointN(
        ST_ExteriorRing(
            'POLYGON((5 0 2, 7 0 4, 7 1 3, 5 1 6, 5 0 1))'), 3));
-- Answer: 3.0

SELECT ST_Z('GEOMETRYCOLLECTION(
               LINESTRING(2 1 0, 1 3 3, 5 2 1),
               MULTIPOINT((4 4 3), (1 1 1), (1 0 2), (0 3 6)),
               POLYGON((1 2 2, 4 2 5, 4 6 3, 1 6 1, 1 2 1)))');
-- Answer: 0.0
```

## See also

* [`ST_ExteriorRing`](../ST_ExteriorRing),
  [`ST_GeometryN`](../ST_GeometryN),
  [`ST_PointN`](../ST_PointN),
  [`ST_X`](../ST_X),
  [`ST_Y`](../ST_Y)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_Z.java" target="_blank">Source code</a>
