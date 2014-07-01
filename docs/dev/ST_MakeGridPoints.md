---
layout: docs
title: ST_MakeGridPoints
category: geom2D/geometry-creation
is_function: true
description: Create a regular grid of points based on a table or a geometry envelope
prev_section: ST_MakeGrid
next_section: ST_MakeLine
permalink: /docs/dev/ST_MakeGridPoints/
---

### Signature

{% highlight mysql %}
tableName[NODE_GEOM, ID, ID_COL, ID_ROW] ST_MakeGridPoints(
    VALUE value, DOUBLE deltaX, DOUBLE deltaY);
{% endhighlight %}

### Description
Calculates a regular grid of Points using the first input value to compute the full extent.
The first argument is either a Geometry or a table. The `deltaX` and `deltaY` cell grid are expressed in a cartesian plane.

<div class="note">
	<h5>The Geometry could be expressed using a subquery as (SELECT the_geom from myTable)</h5>
</div>

### Examples

{% highlight mysql %}
CREATE TABLE input_table(the_geom Geometry);
INSERT INTO input_table VALUES('POLYGON((0 0, 2 0, 2 2, 0 0))');
CREATE TABLE grid AS SELECT * FROM ST_MakeGridPoints(
   'input_table', 1, 1);
SELECT * FROM grid;
--Answer:
-- |    NODE_GEOM    |  ID | ID_COL | ID_ROW |
-- | --------------- | --- | ------ | ------ |
-- | POINT(0.5 0.5)  |   0 |      1 |      1 |
-- | POINT(1.5 0.5)  |   1 |      2 |      1 |
-- | POINT(0.5 1.5)  |   2 |      1 |      2 |
-- | POINT(1.5 1.5)  |   3 |      2 |      2 |

CREATE TABLE grid AS SELECT * FROM ST_MakeGridPoints(
   'POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 1, 1);
SELECT * FROM grid;
--Answer:
-- |    NODE_GEOM    |  ID | ID_COL | ID_ROW |
-- | --------------- | --- | ------ | ------ |
-- | POINT(0.5 0.5)  |   0 |      1 |      1 |
-- | POINT(1.5 0.5)  |   1 |      2 |      1 |
-- | POINT(0.5 1.5)  |   2 |      1 |      2 |
-- | POINT(1.5 1.5)  |   3 |      2 |      2 |

CREATE TABLE input_table(the_geom Geometry);
INSERT INTO input_table VALUES('POLYGON((0 0, 2 0, 2 2, 0 0))');
INSERT INTO input_table VALUES('POLYGON((1 1, 2 2, 1 2, 1 1))');
CREATE TABLE grid AS SELECT * FROM ST_MakeGridPoints((SELECT
   ST_Union(ST_Accum(the_geom)) FROM input_table), 1, 1);
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
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/create/ST_MakeGridPoints.java" target="_blank">Source code</a>

