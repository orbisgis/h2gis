# ST_TriangleSlope

## Signature

```sql
DOUBLE ST_TriangleSlope(GEOMETRY geom);
```

## Description

Computes the slope of the triangle `geom` expressed as a percentage.
Throws an error if `geom` is not a triangle.

<img class="displayed" src="../ST_TriangleSlope_0.png"/>

## Examples

```sql
SELECT ST_TriangleSlope('POLYGON((0 0 0, 2 0 0, 1 1 0, 0 0 0))');
-- Answer: 0

SELECT ST_TriangleSlope('POLYGON((0 0 10, 10 0 1, 5 5 10, 0 0 10))');
-- Answer: 127.27922061357853

SELECT ST_TriangleSlope('POLYGON((0 0 0, 4 0 0, 2 3 6, 0 0 0))');
-- Answer: 200.0

-- We try the function on a square.
SELECT ST_TriangleSlope(
    'POLYGON((0 0 1, 3 0 0, 3 3 4, 0 3 1, 0 0 1))');
-- Exception calling user-defined function:
--     "computeSlope(POLYGON ((0 --> 0, 3 0, 3 3, 0 3, 0 0))):
--     The geometry must be a triangle"
```

<img class="displayed" src="../ST_TriangleSlope_1.png"/>

## See also

* [`ST_TriangleAspect`](../ST_TriangleAspect),
  [`ST_TriangleContouring`](../ST_TriangleContouring),
  [`ST_TriangleDirection`](../ST_TriangleDirection)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/topography/ST_TriangleSlope.java" target="_blank">Source code</a>
