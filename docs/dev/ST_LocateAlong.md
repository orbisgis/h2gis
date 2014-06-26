---
layout: docs
title: ST_LocateAlong
category: geom2D/distance-functions
description: Return a <code>MULTIPOINT</code> containing points along the line segments of a Geometry at a given segment length fraction and offset distance
prev_section: ST_Length
next_section: geom2D/operators
permalink: /docs/dev/ST_LocateAlong/
---

### Signature

{% highlight mysql %}
MULTIPOINT ST_LocateAlong(GEOMETRY geom,
                          DOUBLE segmentLengthFraction,
                          DOUBLE offsetDistance);
{% endhighlight %}

### Description

Returns a `MULTIPOINT` containing points along the line segments of `geom`
matching the specified `segmentLengthFraction` and `offsetDistance`. A positive
offset places points to the left of the segment (with the ordering given
by Coordinate traversal); a negative offset to the right. For areal elements,
only exterior rings are supported.

### Examples

{% highlight mysql %}
SELECT ST_LocateAlong('LINESTRING(1 1, 5 4)', 0.5, 2);
-- Answer: MULTIPOINT((1.8 4.1))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_0.png"/>

{% highlight mysql %}
SELECT ST_LocateAlong('LINESTRING(1 1, 5 1, 5 3)', 0.5, 1);
-- Answer: MULTIPOINT((3 2), (4 2))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_1.png"/>

{% highlight mysql %}
SELECT ST_LocateAlong('POLYGON((1 1, 4 1, 4 3, 1 3, 1 1))', 0.5, -1);
-- Answer: MULTIPOINT((2.5 0), (5 2), (2.5 4), (0 2))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_2.png"/>

{% highlight mysql %}
SELECT ST_LocateAlong('GEOMETRYCOLLECTION(
                           LINESTRING(1 4, 5 4, 5 2),
                           POLYGON((1 1, 4 1, 4 3, 1 3, 1 1)))',
                      2, 1);
-- Answer: MULTIPOINT((2 -1), (-2 2), (6 0), (9 5), (7 2), (3 5))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_3.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/distance/ST_LocateAlong.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/62" target="_blank">#62</a>
