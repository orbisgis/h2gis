---
layout: docs
title: ST_Force3D
category: geom3D/geometry-conversion
is_function: true
description: 2D Geometry &rarr; 3D Geometry
prev_section: geom3D/geometry-conversion
next_section: geom3D/geometry-creation
permalink: /docs/dev/ST_Force3D/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_Force3D(GEOMETRY geom);
{% endhighlight %}

### Description

Converts `geom` to a 3D Geometry by setting its nonexistent
*z*-values to 0.
Entirely 3D Geometries are returned untouched.

### Examples

{% highlight mysql %}
-- No effect on 3D Geometries:
SELECT ST_Force3D('POINT(-10 10 6)');
-- Answer:         POINT(-10 10 6)

SELECT ST_Force3D('POINT(-10 10)');
-- Answer:         POINT(-10 10 0)

SELECT ST_Force3D('LINESTRING(-10 10, 10 10 3)');
-- Answer:         LINESTRING(-10 10 0, 10 10 3)

SELECT ST_Force3D('POLYGON((2 2, 10 0 1, 10 5 1, 0 5 2, 2 2))');
-- Answer:         POLYGON((2 2 0, 10 0 1, 10 5 1, 0 5 2, 2 2 0))
{% endhighlight %}

##### See also

* [`ST_Force2D`](../ST_Force2D), [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_Force3D.java" target="_blank">Source code</a>
