---
layout: docs
title: ST_LocateAlong
category: h2spatial-ext/distance-functions
description: Return a <code>MULTIPOINT</code> containing points along the line segments of the given Geometry matching the specified segment length fraction and offset distance.
prev_section: ST_FurthestCoordinate
next_section: h2spatial-ext/predicates
permalink: /docs/dev/ST_LocateAlong/
---

### Signature

{% highlight mysql %}
MULTIPOINT ST_LocateAlong(GEOMETRY geom, 
                          double segmentLengthFraction, 
                          double offsetDistance);
{% endhighlight %}

### Description
Returns a `MULTIPOINT` containing points along the line segments of the given Geometry matching the specified `segment length fraction` and `offset distance`. A positive offset places the `POINT` to the left of the segment (with the ordering given by Coordinate traversal); a negative offset to the right. For areal elements, only exterior rings are supported.

### Examples

{% highlight mysql %}
SELECT  ST_LocateAlong('LINESTRING(1 1, 4 1, 4 3)', 
    0.5, 1);
-- Anwser: MULTIPOINT((5 2), (2.5 4))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_1.png"/>

{% highlight mysql %}
SELECT  ST_LocateAlong('POLYGON((1 1, 4 1, 4 3, 1 3, 1 1))', 
    0.5, -1);
-- Anwser: MULTIPOINT((2.5 0), (5 2), (2.5 4), (0 2))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_2.png"/>

{% highlight mysql %}
SELECT ST_LocateAlong('GEOMETRYCOLLECTION(
                       LINESTRING(1 3, 4 3, 4 1),
                       POLYGON((1 1, 4 1, 4 3, 1 3, 1 1)))', 
    2, 1);
-- Anwser: MULTIPOINT((2 -1), (5 -1), (-2 2), (7 4), (7 2), 
--         (3 5))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_3.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/distance/ST_LocateAlong.java" target="_blank">Source code</a>
