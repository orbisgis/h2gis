---
layout: docs
title: ST_MakeGrid
category: Geometry2D/geometry-creation
description: Create a regular grid based on a table or a geometry envelope
prev_section: ST_MakeEnvelope
next_section: ST_MakeGridPoints
permalink: /docs/dev/ST_MakeGrid/
---

### Signature

{% highlight mysql %}
tableName[NODE_GEOM, ID, ID_COL, ID_ROW] ST_MakeGrid(
    Value value, double deltaX, double deltaY);
{% endhighlight %}

### Description
Calculates a regular grid. The first argument is either a Geometry or a table. The `deltaX` and `deltaY` cell grid are expressed in a cartesian plane. 

<div class="note">
	<h5>The Geometry could be expressed using a subquery as (SELECT the_geom from myTable)</h5>
</div>


### Examples

{% highlight mysql %}
CREATE TABLE grid AS SELECT * FROM st_makegrid(
   'POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 1, 1);
SELECT * FROM grid;
-- Answer: 
-- |             NODE_GEOM              |  ID | ID_COL | ID_ROW |
-- | ---------------------------------- | --- | ------ | ------ |
-- | POLYGON((0 0, 1 0, 1 1, 0 1, 0 0)) |   0 |      1 |      1 |
-- | POLYGON((1 0, 2 0, 2 1, 1 1, 1 0)) |   1 |      2 |      1 |
-- | POLYGON((0 1, 1 1, 1 2, 0 2, 0 1)) |   2 |      1 |      2 |
-- | POLYGON((1 1, 2 1, 2 2, 1 2, 1 1)) |   3 |      2 |      2 |

CREATE TABLE input_table(the_geom Geometry);
INSERT INTO input_table VALUES('POLYGON((0 0, 2 0, 2 2, 0 0))');
CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1,
                                               1);
SELECT * FROM grid;
-- Answer: 
-- |             NODE_GEOM              |  ID | ID_COL | ID_ROW |
-- | ---------------------------------- | --- | ------ | ------ |
-- | POLYGON((0 0, 1 0, 1 1, 0 1, 0 0)) |   0 |      1 |      1 |
-- | POLYGON((1 0, 2 0, 2 1, 1 1, 1 0)) |   1 |      2 |      1 |
-- | POLYGON((0 1, 1 1, 1 2, 0 2, 0 1)) |   2 |      1 |      2 |
-- | POLYGON((1 1, 2 1, 2 2, 1 2, 1 1)) |   3 |      2 |      2 |

CREATE TABLE input_table(the_geom Geometry);
INSERT INTO input_table VALUES('POLYGON((0 0, 2 0, 2 2, 0 0))');
INSERT INTO input_table VALUES('POLYGON((1 1, 2 2, 1 2, 1 1 ))');
CREATE TABLE grid AS SELECT * FROM st_makegrid((SELECT 
   ST_Union(ST_Accum(the_geom)) FROM input_table), 1, 1);
SELECT * FROM grid;
-- Answer: 
-- |             NODE_GEOM              |  ID | ID_COL | ID_ROW |
-- | ---------------------------------- | --- | ------ | ------ |
-- | POLYGON((0 0, 1 0, 1 1, 0 1, 0 0)) |   0 |      1 |      1 |
-- | POLYGON((1 0, 2 0, 2 1, 1 1, 1 0)) |   1 |      2 |      1 |
-- | POLYGON((0 1, 1 1, 1 2, 0 2, 0 1)) |   2 |      1 |      2 |
-- | POLYGON((1 1, 2 1, 2 2, 1 2, 1 1)) |   3 |      2 |      2 |
{% endhighlight %}

<img class="displayed" src="../ST_MakeGrid_1.png"/>

##### See also

* [`ST_MakeGridPoints`](../ST_MakeGridPoints)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/create/ST_MakeGrid.java" target="_blank">Source code</a>
