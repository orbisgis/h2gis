---
layout: docs
title: ST_Densify
category: h2spatial-ext/edit-geometries
description: Return a Geometry with more vertex
prev_section: ST_AddZ
next_section: ST_Interpolate3DLine
permalink: /docs/dev/ST_Densify/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_Densify(GEOMETRY geom, double tolerance);
{% endhighlight %}

### Description
Densifies a `GEOMETRY` by inserting extra vertices along the line segments contained in the `GEOMETRY` using the given distance `tolerance`.

### Examples

{% highlight mysql %}
SELECT ST_Densify('POINT(14 2)', 10);
-- Answer: POINT(14 2)

SELECT ST_Densify('LINESTRING(1 11, 8 1)', 2);
-- Answer: LINESTRING(1 11, 2 9.5714, 3 8.1428, 
--                    4 6.7142, 5 5.2857, 6 3.85714, 
--                    7 2.4285, 8 1)

SELECT ST_Densify('LINESTRING(1 11, 8 1)', 10);
-- Answer: LINESTRING(1 11, 4.5 6, 8 1)
{% endhighlight %}

<img class="displayed" src="../ST_Densify.png"/>

{% highlight mysql %}
SELECT ST_Densify('POLYGON((2 0, 2 8, 4 8, 4 0, 2 0))', 4.5)
-- Answer: POLYGON((2 0, 2 4, 2 8, 4 8, 4 4, 4 0, 2 0))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_Densify.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
