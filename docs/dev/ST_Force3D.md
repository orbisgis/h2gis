---
layout: docs
title: ST_Force3D
category: geom3D/geometry-conversion
is_function: true
description: Convert a XY geometry to XYZ
prev_section: geom3D/geometry-conversion
next_section: geom3D/geometry-creation
permalink: /docs/dev/ST_Force3D/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_Force3D(GEOMETRY geom);
{% endhighlight %}

### Description
Returns a Geometry in 3-dimensional. The output Geometry will have the X, Y and Z coordinates.
If a given Geometry has no Z component, then a 0 Z coordinate is tacked on.

### Examples

{% highlight mysql %}
SELECT ST_Force3D('POINT(-10 10 6)');
-- Answer: POINT(-10 10 6)

SELECT ST_Force3D('POINT(-10 10)');
-- Answer: POINT(-10 10 0)

SELECT ST_Force3D('LINESTRING(-10 10, 10 10 3)');
-- Answer: LINESTRING(-10 10 0, 10 10 3)

SELECT ST_Force3D('POLYGON((2 2, 10 0 1, 10 5 1, 0 5 2, 2 2))');
-- Answer: POLYGON((2 2 0, 10 0 1, 10 5 1, 0 5 2, 2 2 0))
{% endhighlight %}

##### See also

* [`ST_Force2D`](../ST_Force2D), [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/irstv/H2GIS/blob/51910b27b5dc2b3b4353bb43a683f8649628ea8d/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/convert/ST_Force3D.java" target="_blank">Source code</a>

