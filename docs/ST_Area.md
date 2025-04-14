# ST_Area

## Signature

```sql
DOUBLE ST_Area(GEOMETRY geom);
DOUBLE ST_Area(GEOMETRYCOLLECTION geom);
```

## Description

Returns the area of `geom`.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_Area('POINT(0 12)');
-- Answer: 0.0

SELECT ST_Area('LINESTRING(5 4, 1 1, 3 4, 4 5)');
-- Answer: 0.0

SELECT ST_Area('POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))');
-- Answer: 100.0

SELECT ST_Area('MULTIPOLYGON(((0 0, 10 0, 10 10, 0 10, 0 0),
                              (5 4, 1 1, 3 4, 4 5, 5 4)))');
-- Answer: 96.0

SELECT ST_Area('GEOMETRYCOLLECTION(
                  LINESTRING(5 4, 1 1, 3 4, 4 5),
                  POINT(0 12),
                  POLYGON((0 0, 10 0, 10 10, 0 10, 0 0)),
                  POLYGON((5 4, 1 1, 3 4, 4 5, 5 4)))');
-- Answer: 104.0
```

## See also

* [`ST_3DArea`](../ST_3DArea)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_Area.java" target="_blank">Source code</a>
