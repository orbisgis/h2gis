---
layout: docs
title: ST_AddPoint
category: h2spatial-ext/edit-geometries
description: Return a Geometry based on an existing Geometry with a specific <code>POINT</code> as a new vertex.
prev_section: h2spatial-ext/edit-geometries
next_section: ST_AddZ
permalink: /docs/dev/ST_AddPoint/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_AddPoint(GEOMETRY geom, POINT point);
GEOMETRY ST_AddPoint(GEOMETRY geom, POINT point, double tolerance);
{% endhighlight %}

### Description
Returns a new `GEOMETRY` based on an existing one, with a specific `POINT` as a new vertex.
A `tolerance` could be set to snap the POINT to the GEOMETRY. A default distance 10E-6 is used to snap the input point.

### Examples

{% highlight mysql %}
SELECT ST_AddPoint('POINT(0 0)', 'POINT(1 1)');
-- Answer: null

SELECT ST_AddPoint('MULTIPOINT((0 0))', 'POINT(1 1)');
-- Answer: MULTIPOINT((0 0), (1 1))

SELECT ST_AddPoint('LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8)', 'POINT(1.5 4)', 4);
-- Answer: LINESTRING (0 8, 1 8, 1.5 8, 3 8, 8 8, 10 8, 20 8)

SELECT ST_AddPoint('POLYGON ((118 134, 118 278, 266 278, 266 134, 118 134 ))', 'POINT(196 278)', 4);
-- Answer: POLYGON ((118 134, 118 278, 196 278, 266 278, 266 134, 118 134))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_AddPoint.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
