---
layout: docs
title: ST_OrderingEquals
category: geom2D/predicates
is_function: true
description: Returns <code>TRUE</code> if Geometry A equals Geometry B and their coordinates and component Geometries are listed in the same order
prev_section: ST_Intersects
next_section: ST_Overlaps
permalink: /docs/1.5.0/ST_OrderingEquals/
---

### Signature

{% highlight mysql %}
BOOLEAN ST_OrderingEquals(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns `TRUE` if `geomA` and `geomB` are equal and their
coordinates and component Geometries are listed in the same order.
The condition is stronger than [`ST_Equals`](../ST_Equals).

### Examples

{% highlight mysql %}
-- The same:
SELECT ST_OrderingEquals('LINESTRING(0 0 1, 0 0, 10 10 3)',
                         'LINESTRING(0 0 1, 0 0, 10 10 3)');
-- Answer: TRUE

-- Different:
SELECT ST_OrderingEquals('LINESTRING(0 0, 10 10)',
                         'LINESTRING(0 0, 5 5, 10 10)');
-- Answer: FALSE

-- The same, but with opposite vertex order:
SELECT ST_OrderingEquals('POLYGON(0 0, 10 10, 10 5, 0 0)',
                         'POLYGON(0 0, 10 5, 10 10, 0 0)');
-- Answer: FALSE

-- Different:
SELECT ST_OrderingEquals('LINESTRING(0 0 1, 0 0, 10 10)',
                         'LINESTRING(0 0, 0 0, 10 10)');
-- Answer: FALSE

-- The same, but component POLYGONs are listed in opposite order:
SELECT ST_OrderingEquals('MULTIPOLYGON(((0 0, 10 10, 10 5, 0 0)),
                                       ((1 1, 2 2, 2 1, 1 1)))',
                         'MULTIPOLYGON(((1 1, 2 2, 2 1, 1 1)),
                                       ((0 0, 10 10, 10 5, 0 0)))');
-- Answer: FALSE
{% endhighlight %}

##### See also

* [`ST_Equals`](../ST_Equals), [`ST_Reverse`](../ST_Reverse)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/predicates/ST_OrderingEquals.java" target="_blank">Source code</a>
