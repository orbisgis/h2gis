---
layout: docs
title: ST_Interpolate3DLine
category: h2spatial-ext/edit-geometries
description: Return a Geometry with a interpolation of z values.
prev_section: ST_Densify
next_section: ST_MultiplyZ
permalink: /docs/dev/ST_Interpolate3DLine/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_Interpolate3DLine(GEOMETRY geom);
{% endhighlight %}

### Description
Interpolate the z values of a `LINESTRING` or `MULTILINESTRING` based on the start and the end z values. 
If z input value is equal to NaN, Nan is returned in the output Geometry.

### Examples

{% highlight mysql %}
SELECT ST_Interpolate3DLine('POINT(0 0 0)');
-- Answer: null

SELECT ST_Interpolate3DLine('POLYGON((2 0 1, 2 8 0, 4 8, 
                                      4 0, 2 0))');
-- Answer: null

SELECT ST_Interpolate3DLine('LINESTRING(0 8, 1 8, 3 8)');
-- Answer: LINESTRING(0 8, 1 8, 3 8)
SELECT ST_Z(ST_PointN(ST_Interpolate3DLine('LINESTRING(0 8, 1 8,
                                                       3 8)'),1));
-- Answer: NaN

SELECT ST_Interpolate3DLine('LINESTRING(0 0 1, 5 0 , 10 0 10)');
-- Answer: LINESTRING(0 0 1, 5 0 5.5, 10 0 10)

SELECT ST_Interpolate3DLine('MULTILINESTRING((0 0 0, 5 0 , 
                                              10 0 10),
                                             (0 0 0, 50 0, 
                                              100 0 100))');
-- Answer: MULTILINESTRING((0 0 0, 5 0 5, 10 0 10), 
--                          (0 0 0, 50 0 50, 100 0 100))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_Interpolate3DLine.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
