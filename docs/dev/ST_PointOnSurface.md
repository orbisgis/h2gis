---
layout: docs
title: ST_PointOnSurface
category: h2spatial/properties
description: Return a <code>POINT</code> that lie on the surface of a Geometry
prev_section: ST_PointN
next_section: ST_SRID
permalink: /docs/dev/ST_PointOnSurface/
---

### Signature

{% highlight mysql %}
POINT ST_InteriorPoint(GEOMETRY geom);
{% endhighlight %}

### Description

Returns a `POINT` that lie on the surface of a Geometry. If it's impossible to calculate the surface of a Geometry, the point may lie on the boundary of the Geometry. 
The returned point is always the same for the same Geometry.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_PointOnSurface('POINT(1 5)');
-- Answer: POINT(1 5)

SELECT ST_PointOnSurface('MULTIPOINT((4 4), (1 1), (1 0), 
                                     (0 3)))');
-- Answer: POINT(1 1)

SELECT ST_PointOnSurface('LINESTRING(-1 5, 0 10)');
-- Answer: POINT(0 10)

SELECT ST_PointOnSurface('POLYGON((0 0, 0 5, 5 5, 5 0, 0 0))');
-- Answer: POINT(2.5 2.5)

SELECT ST_PointOnSurface('GEOMETRYCOLLECTION(
                              POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)), 
                              LINESTRING(2 6, 6 2), 
                              MULTIPOINT((4 4), (1 1), (0 3)))');
-- Answer: POINT(2.5 4)
{% endhighlight %}

##### Comparison with [`ST_Centroid`](../ST_Centroid)

{% include centroid-pointonsurface-cf.html %}

##### See also

* [`ST_Centroid`](../ST_Centroid)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_PointOnSurface.java" target="_blank">Source code</a>
