---
layout: docs
title: ST_OrderingEquals
category: geom2D/predicates
is_function: true
description: Return true if the given Geometries represent the same Geometry and points are in the same directional order
prev_section: ST_Intersects
next_section: ST_Overlaps
permalink: /docs/dev/ST_OrderingEquals/
---

### Signature

{% highlight mysql %}
BOOLEAN ST_OrderingEquals(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description
Returns true if the given Geometries represent the same Geometry and points are in the same directional order.

### Examples

{% highlight mysql %}
SELECT ST_OrderingEquals('LINESTRING(0 0 1, 0 0, 10 10 3)',
                         'LINESTRING(0 0 1, 0 0, 10 10 3)');
-- Answer: TRUE

SELECT ST_OrderingEquals('LINESTRING(0 0, 10 10)',
                         'LINESTRING(0 0, 10 10)');
-- Answer: TRUE

SELECT ST_OrderingEquals('LINESTRING(0 0, 10 10)',
                         'LINESTRING(0 0, 5 5, 10 10)');
-- Answer: FALSE

SELECT ST_OrderingEquals('POLYGON(0 0, 10 10, 10 5, 0 0)',
                         'POLYGON(0 0, 10 5, 10 10, 0 0)');
-- Answer: FALSE

SELECT ST_OrderingEquals('LINESTRING(0 0 1, 0 0, 10 10)',
                         'LINESTRING(0 0, 0 0, 10 10)');
-- Answer: FALSE

SELECT ST_OrderingEquals('MULTIPOLYGON(((0 0, 10 10, 10 5, 0 0)),
                                       ((1 1, 2 2, 2 1, 1 1)))',
                         'MULTIPOLYGON(((1 1, 2 2, 2 1, 1 1)),
                                       ((0 0, 10 10, 10 5, 0 0)))');
-- Answer: FALSE
{% endhighlight %}

##### See also

* [`ST_Equals`](../ST_Equals), [`ST_Reverse`](../ST_Reverse)
* <a href="https://github.com/irstv/H2GIS/blob/b3b4d698d2d8da9e442fb13231c60b50d8d532ab/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_OrderingEquals.java" target="_blank">Source code</a>

