---
layout: docs
title: ST_MakePolygon
category: Geometry2D/geometry-creation
description: <code>LINESTRING</code> &rarr; <code>POLYGON</code>
prev_section: ST_MakePoint
next_section: ST_MinimumDiameter
permalink: /docs/dev/ST_MakePolygon/
---

### Signatures

{% highlight mysql %}
POLYGON ST_MakePolygon(GEOMETRY shell);
POLYGON ST_MakePolygon(GEOMETRY shell, GEOMETRY holes)
{% endhighlight %}

### Description
Creates a Polygon formed by the given `shell` and optionally `holes`.

<div class="note">
	<h5>Input `geom` must be closed `LINESTRING`</h5>
</div>

### Examples

{% highlight mysql %}
SELECT ST_MakePolygon('POINT(100 250)');
-- Answer: Exception calling user-defined function: 
--    "makePolygon(POINT(100 250)): Only support linestring."

SELECT ST_MakePolygon('LINESTRING(100 250, 100 350, 200 350, 
                                  200 250)');
-- Answer: Exception calling user-defined function: 
--    "makePolygon(LINESTRING(100 250, 100 350, 200 350, 200 250))
--    : The linestring must be closed."

SELECT ST_MakePolygon('LINESTRING(100 250, 100 350, 200 350, 
                                  200 250, 100 250)');
-- Answer: POLYGON((100 250, 100 350, 200 350, 200 250, 100 250))

SELECT ST_MakePolygon('LINESTRING(0 5, 4 5, 4 0, 0 0, 0 5)', 
                      'LINESTRING(1 1, 1 2, 2 2, 2 1, 1 1)');
-- Answer: POLYGON((0 5, 4 5, 4 0, 0 0, 0 5), 
--                 (1 1, 1 2, 2 2, 2 1, 1 1))
{% endhighlight %}

<img class="displayed" src="../ST_MakePolygon_1.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/create/ST_MakePolygon.java" target="_blank">Source code</a>

