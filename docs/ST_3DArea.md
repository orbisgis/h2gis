# ST_3DArea

## Signature

```sql
DOUBLE ST_Area(GEOMETRY geom);
```

## Description

Compute the 3D area of a `polygon` or a `multipolygon` derived from a 3D triangular decomposition.
In the case of a 2D Geometry, `ST_3DArea` returns the same value as `ST_Area`.

Area is measured in the units of the spatial reference system.

## Examples

```sql
SELECT ST_3DArea('POLYGON((0 0 3, 10 0 4, 10 10 6, 0 10 2, 0 0 3))');
-- Answer: 105.27

SELECT ST_3DArea('POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))');
-- Answer: 100.0

SELECT ST_Area('MULTIPOLYGON(((0 0 3, 10 0 4, 10 10 6, 0 10 2, 0 0 3),
                              (5 4 1, 1 1 2, 3 4 3, 4 5 2, 5 4 1)))');
-- Answer: 111.88

SELECT ST_Area('GEOMETRYCOLLECTION(
                  LINESTRING(5 4, 1 1, 3 4, 4 5),
                  POINT(0 12),
                  POLYGON((0 0, 10 0, 10 10, 0 10, 0 0)),
                  POLYGON((5 4, 1 1, 3 4, 4 5, 5 4)))');
-- Answer: XXXX

SELECT ST_3DArea('LINESTRING(5 4, 1 1, 3 4, 4 5)');
-- Answer: 0.0
```

## See also

* [`ST_Area`](../ST_Area), [`ST_Length`](../ST_Length), [`ST_3DLength`](../ST_3DLength)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_3DArea.java" target="_blank">Source code</a>
