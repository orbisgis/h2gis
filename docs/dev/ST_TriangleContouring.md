---
layout: docs
title: ST_TriangleContouring
category: geom3D/topography
is_function: true
description: Split triangles into smaller triangles according to classes
prev_section: ST_TriangleAspect
next_section: ST_TriangleDirection
permalink: /docs/dev/ST_TriangleContouring/
---

### Signatures

{% highlight mysql %}
-- Return type: tableName[GEOM, IDISO]
ST_TriangleContouring(VARCHAR tableName,
                      INT varArgs1, INT varArgs2, INT varArgs3, ...);
ST_TriangleContouring(VARCHAR tableName,
                      VARCHAR z1, VARCHAR z2, VARCHAR z3,
                      INT varArgs1, INT varArgs2, INT varArgs3, ...);
{% endhighlight %}

### Description

Splits the triangles contained in table `tableName` into smaller
triangles according to certain *z*-values with respect to the
classes given by the various `varArgs` arguments. Throws an error if
a Geometry is found which is not a triangle.

If column names `z1`, `z2` and `z3` are not specified, the
*z*-values of each Geometry (in order of appearance) are used.
Otherwise, the values contained in these columns are used.

The various `varArgs` arguments (you may provide as many as you
like) define the classes according to which the triangles are split.
The interval from negative infinity to `varArgs1` is assigned an
`IDISO` of `0`, from `varArgs1` to `varArgs2` gets an `IDISO` of 1,
etc.

<img class="displayed" src="../ST_TriangleContouring_0.png"/>

### Examples

{% highlight mysql %}
CREATE TABLE TIN(GEOM GEOMETRY) AS
    SELECT 'POLYGON((0 0 1, 3 0 0, 3 3 4, 0 0 1))';
SELECT * FROM ST_TriangleContouring('TIN', 2, 3, 4);
-- Answer:
-- |                    GEOM                   | IDISO |
-- | --------------------------------------------- | ----- |
-- | POLYGON((3 1.5 2, 1 1 2, 0 0 1, 3 1.5 2))     |     0 |
-- | POLYGON((3 1.5 2, 0 0 1, 3 0 0, 3 1.5 2))     |     0 |
-- | POLYGON((3 2.25 3, 2 2 3, 3 1.5 2, 3 2.25 3)) |     1 |
-- | POLYGON((2 2 3, 1 1 2, 3 1.5 2, 2 2 3))       |     1 |
-- | POLYGON((3 2.25 3, 3 3 4, 2 2 3, 3 2.25 3))   |     2 |
{% endhighlight %}

<img class="displayed" src="../ST_TriangleContouring_1.png"/>

{% highlight mysql %}
-- In this example, the bottom right corner of the triangle is
-- deleted since it is assigned a z-value (of 6) which is greater
-- than the largest class value (5).
DROP TABLE IF EXISTS TIN;
CREATE TABLE TIN(GEOM GEOMETRY, M1 DOUBLE, M2 INT, M3 DOUBLE) AS
    SELECT 'POLYGON((0 0 1, 3 0 0, 3 3 4, 0 0 1))',
           1.0, 6, 4.0;
SELECT * FROM ST_TriangleContouring('TIN', 'm1', 'm2', 'm3', 2, 3, 5);
-- Answer:
-- |               GEOM               |  m1 |  m2 |  m3 | IDISO |
-- | ------------------------------------ | --- | --- | --- | ----- |
-- | POLYGON((0.6 0 0.8, 1 1 2, 0 0 1,    | 1.0 |   6 | 4.0 |     0 |
-- |          0.6 0 0.8))                 |     |     |     |       |
-- | POLYGON((1.8 1.5 2.4, 2 2 3,         | 1.0 |   6 | 4.0 |     1 |
-- |          0.6 0 0.8, 1.8 1.5 2.4))    |     |     |     |       |
-- | POLYGON((2 2 3, 1 1 2, 0.6 0 0.8,    | 1.0 |   6 | 4.0 |     1 |
-- |          2 2 3))                     |     |     |     |       |
-- | POLYGON((1.2 0 0.6, 1.8 1.5 2.4,     | 1.0 |   6 | 4.0 |     1 |
-- |          0.6 0 0.8, 1.2 0 0.6))      |     |     |     |       |
-- | POLYGON((1.8 1.5 2.4, 3 3 4, 2 2 3,  | 1.0 |   6 | 4.0 |     2 |
-- |          1.8 1.5 2.4))               |     |     |     |       |
-- | POLYGON((1.2 0 0.6, 3 3 4,           | 1.0 |   6 | 4.0 |     2 |
-- |          1.8 1.5 2.4, 1.2 0 0.6))    |     |     |     |       |
-- | POLYGON((2.4 0 0.2, 3 1.5 2,         | 1.0 |   6 | 4.0 |     2 |
-- |          1.2 0 0.6, 2.4 0 0.2))      |     |     |     |       |
-- | POLYGON((3 1.5 2, 3 3 4, 1.2 0 0.6,  | 1.0 |   6 | 4.0 |     2 |
-- |          3 1.5 2))                   |     |     |     |       |

-- An error is thrown if the Geometry is not a triangle.
CREATE TABLE TIN(GEOM GEOMETRY) AS
    SELECT 'POLYGON((0 0 1, 3 0 0, 3 3 4, 0 3 1, 0 0 1))';
SELECT * FROM ST_TriangleContouring('TIN', 2, 3, 4);
-- Answer: Invalid geometry input, got
--         POLYGON ((0 0, 3 0, 3 3, 0 3, 0 0))
{% endhighlight %}

<img class="displayed" src="../ST_TriangleContouring_2.png"/>

##### See also

* [`ST_TriangleAspect`](../ST_TriangleAspect),
  [`ST_TriangleDirection`](../ST_TriangleDirection),
  [`ST_TriangleSlope`](../ST_TriangleSlope)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/topography/ST_TriangleContouring.java" target="_blank">Source code</a>
