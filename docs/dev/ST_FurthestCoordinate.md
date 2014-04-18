---
layout: docs
title: ST_FurthestCoordinate
category: Geometry2D/distance-functions
description: Compute the furthest coordinate(s) contained in a Geometry starting from a <code>POINT</code>
prev_section: ST_ClosestPoint
next_section: ST_Length
permalink: /docs/dev/ST_FurthestCoordinate/
---

### Signatures

{% highlight mysql %}
{POINT,MULTIPOINT} ST_FurthestCoordinate(POINT point, GEOMETRY geom);
{% endhighlight %}

### Description

Returns the coordinate of `geom` furthest from `point` using 2D distances
(z-coordinates are ignored).

{% include uniqueness.html rel='furthest' type='coordinate' %}

### Examples

{% highlight mysql %}
SELECT ST_FurthestCoordinate('POINT(0 0)', 
                             'POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))');
-- Answer: POINT(10 5)
{% endhighlight %}

<img class="displayed" src="../ST_FurthestCoordinate_1.png"/>

{% highlight mysql %}
SELECT ST_FurthestCoordinate('POINT(5 2.5)', 
                             'LINESTRING(3 1, 2 2, 2 4, 4 5)');
-- Answer: POINT(2 4)
{% endhighlight %}

<img class="displayed" src="../ST_FurthestCoordinate_2.png"/>

{% highlight mysql %}
SELECT ST_FurthestCoordinate('POINT(5 2.5)', 
                             'POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))');
-- Answer: MULTIPOINT((10 5), (0 0), (0 5), (10 0))
{% endhighlight %}

<img class="displayed" src="../ST_FurthestCoordinate_3.png"/>

##### See also

* [`ST_ClosestCoordinate`](../ST_ClosestCoordinate)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/distance/ST_FurthestCoordinate.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/62" target="_blank">#62</a>
