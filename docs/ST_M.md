# ST_M

## Signature

```sql
DOUBLE ST_M(GEOMETRY geom);
```

## Description

Returns the m-value of the first coordinate of `geom`.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_M('LINESTRING M(2 1 0, 1 3 3, 5 2 1)');
-- Answer: 0.0
```

```sql
SELECT ST_M('POLYGON M((5 0 2, 7 0 4, 7 1 3, 5 1 6, 5 0 1))');
-- Answer: 2.0
```

```sql
SELECT ST_M(
    ST_PointN(
        ST_ExteriorRing(
            'POLYGON M((5 0 2, 7 0 4, 7 1 3, 5 1 6, 5 0 1))'), 3));
-- Answer: 3.0
```

```sql
SELECT ST_M('GEOMETRYCOLLECTION(
               LINESTRING M(2 1 0, 1 3 3, 5 2 1),
               MULTIPOINT M((4 4 3), (1 1 1), (1 0 2), (0 3 6)),
               POLYGON M((1 2 2, 4 2 5, 4 6 3, 1 6 1, 1 2 1)))');
-- Answer: 0.0
```

## See also

* [`ST_X`](../ST_X), [`ST_Y`](../ST_Y), [`ST_Z`](../ST_Z)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_M.java" target="_blank">Source code</a>
