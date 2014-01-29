---
layout: docs
title: ST_ToMultiLine
category: h2spatial-ext/geometry-conversion
description: Construct a <code>MULTILINESTRING</code> from the given Geometry's coordinates
prev_section: ST_Holes
next_section: ST_ToMultiPoint
permalink: /docs/dev/ST_ToMultiLine/
---

### Signature

{% highlight mysql %}
MULTILINESTRING ST_ToMultiLine(GEOMETRY geom);
{% endhighlight %}

### Description
 `ST_ToMultiLine` constructs a `MULTILINESTRING` from the given `GEOMETRY`'s coordinates. Returns `MULTILINESTRING EMPTY` for Geometries of dimension 0.

### Examples

{% highlight mysql %}
SELECT ST_ToMultiLine('LINESTRING EMPTY');
-- Answer: MULTILINESTRING EMPTY	

SELECT ST_ToMultiLine('POINT(2 4)');
-- Answer: MULTILINESTRING EMPTY	

SELECT ST_ToMultiLine('LINESTRING(5 5, 1 2, 3 4, 99 3)');
-- Answer:MULTILINESTRING ((5 5, 1 2, 3 4, 99 3))	

SELECT ST_ToMultiLine('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))');
-- Answer: MULTILINESTRING ((0 0, 10 0, 10 5, 0 5, 0 0))	

SELECT ST_ToMultiLine('POLYGON ((0 0, 10 0, 10 6, 0 6, 0 0), 
    (1 1, 2 1, 2 5, 1 5, 1 1))');
-- Answer: MULTILINESTRING ((0 0, 10 0, 10 5, 0 5, 0 0), 
--    (1 1, 2 1, 2 4, 1 4, 1 1))
{% endhighlight %}

<img class="displayed" src="../ST_ToMultiLine1.png"/>

{% highlight mysql %}
SELECT ST_ToMultiLine('
    MULTIPOLYGON(((28 26,28 0,84 0,84 42,28 26), 
    (52 18,66 23,73 9,48 6,52 18)), 
    ((59 18,67 18,67 13,59 13,59 18)))');
-- Answer: MULTILINESTRING ((28 26, 28 0, 84 0, 84 42, 28 26), 
--    (52 18, 66 23, 73 9, 48 6, 52 18), 
--    (59 18, 67 18, 67 13, 59 13, 59 18))	

SELECT ST_ToMultiLine('GEOMETRYCOLLECTION(
   LINESTRING(1 4 3, 10 7 9, 12 9 22), 
   POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))');
-- Answer: MULTILINESTRING ((1 4, 10 7, 12 9), (1 1, 3 1, 3 2, 1 2, 1 1))
{% endhighlight %}
<img class="displayed" src="../ST_ToMultiLine2.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/convert/ST_ToMultiLine.java" target="_blank">Source code</a>
