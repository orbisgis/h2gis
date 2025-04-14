# ST_Perimeter

## Signature

```sql
DOUBLE ST_Perimeter(GEOMETRY poly);
```

## Description

Returns the perimeter of a `polygon` or a `multipolygon`.

Note that (multi)polygons within `geometrycollection` are accepted.

Perimeter is measured in the units of the spatial reference system.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_Perimeter('POLYGON((1 2, 4 2, 4 6, 1 6, 1 2))');
-- Answer: 14.0

SELECT ST_Perimeter('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)),
                                  ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: 20.0

SELECT ST_Perimeter('GEOMETRYCOLLECTION(
                    MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                    LINESTRING(2 1, 1 3, 5 2),
                    POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 14.0

SELECT ST_Perimeter('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 0
```

## See also

* [`ST_3DPerimeter`](../ST_3DPerimeter), [`ST_Length`](../ST_Length), [`ST_3DLength`](../ST_3DLength)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_Perimeter.java" target="_blank">Source code</a>
