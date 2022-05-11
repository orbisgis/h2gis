---
layout: docs
title: ST_MakeGridPoints
category: geom2D/geometry-creation
is_function: true
description: Calculate a regular grid of <code>POINT</code>s based on a Geometry or a table of Geometries
prev_section: ST_MakeGrid
next_section: ST_MakeLine
permalink: /docs/dev/ST_MakeGridPoints/
---

### Signature

{% highlight mysql %}
TABLE[NODE_GEOM, ID, ID_COL, ID_ROW]
    ST_MakeGridPoints(GEOMETRY geom,
                      DOUBLE deltaX, DOUBLE deltaY);
TABLE[NODE_GEOM, ID, ID_COL, ID_ROW]
    ST_MakeGridPoints(VARCHAR tableName,
                      DOUBLE deltaX, DOUBLE deltaY);
{% endhighlight %}

### Description

Calculates a regular grid of `POINT`s based on a single Geometry
`geom` or a table `tableName` of Geometries with `deltaX` and
`deltaY` as offsets in the Cartesian plane.

### Examples

{% highlight mysql %}
-- Using a Geometry:
CREATE TABLE grid AS SELECT * FROM
    ST_MakeGridPoints('POLYGON((0 0, 2 0, 2 2, 0 0))'::GEOMETRY,
                      1, 1);
SELECT * FROM grid;
--Answer:
-- |    NODE_GEOM    |  ID | ID_COL | ID_ROW |
-- | --------------- | --- | ------ | ------ |
-- | POINT(0.5 0.5)  |   0 |      1 |      1 |
-- | POINT(1.5 0.5)  |   1 |      2 |      1 |
-- | POINT(0.5 1.5)  |   2 |      1 |      2 |
-- | POINT(1.5 1.5)  |   3 |      2 |      2 |

-- Using a table:
CREATE TABLE TEST(GEOM GEOMETRY);
INSERT INTO TEST VALUES ('POLYGON((0 0, 2 0, 2 2, 0 0))');
CREATE TABLE grid AS SELECT * FROM
    ST_MakeGridPoints('TEST', 1, 1);
SELECT * FROM grid;
--Answer:
-- |    NODE_GEOM    |  ID | ID_COL | ID_ROW |
-- | --------------- | --- | ------ | ------ |
-- | POINT(0.5 0.5)  |   0 |      1 |      1 |
-- | POINT(1.5 0.5)  |   1 |      2 |      1 |
-- | POINT(0.5 1.5)  |   2 |      1 |      2 |
-- | POINT(1.5 1.5)  |   3 |      2 |      2 |

-- Using a subquery to construct a Geometry:
CREATE TABLE TEST2(GEOM GEOMETRY);
INSERT INTO TEST2 VALUES
    ('POLYGON((0 0, 2 0, 2 2, 0 0))'),
    ('POLYGON((1 1, 2 2, 1 2, 1 1))');
CREATE TABLE grid AS SELECT * FROM
    ST_MakeGridPoints(
        (SELECT ST_Union(ST_Accum(GEOM)) FROM TEST2),
        1, 1);
SELECT * FROM grid;
--Answer:
-- |    NODE_GEOM    |  ID | ID_COL | ID_ROW |
-- | --------------- | --- | ------ | ------ |
-- | POINT(0.5 0.5)  |   0 |      1 |      1 |
-- | POINT(1.5 0.5)  |   1 |      2 |      1 |
-- | POINT(0.5 1.5)  |   2 |      1 |      2 |
-- | POINT(1.5 1.5)  |   3 |      2 |      2 |
{% endhighlight %}

<img class="displayed" src="../ST_MakeGridPoints_1.png"/>

##### See also

* [`ST_MakeGrid`](../ST_MakeGrid)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/create/ST_MakeGridPoints.java" target="_blank">Source code</a>

