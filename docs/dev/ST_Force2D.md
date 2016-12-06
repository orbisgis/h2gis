---
layout: docs
title: ST_Force2D
category: geom2D/geometry-conversion
is_function: true
description: 3D Geometry &rarr; 2D Geometry
prev_section: ST_AsWKT
next_section: ST_GeomFromGML
permalink: /docs/dev/ST_Force2D/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_Force2D(GEOMETRY geom);
{% endhighlight %}

### Description

Converts a 3D Geometry to a 2D Geometry by deleting the *z*-value of
each coordinate if it exists.

### Examples

{% highlight mysql %}
-- No effect on 2D Geometries:
SELECT ST_Force2D('POINT(-10 10)');
-- Answer:         POINT(-10 10)

SELECT ST_Force2D('POINT(-10 10 6)');
-- Answer:         POINT(-10 10)

SELECT ST_Force2D('LINESTRING(-10 10 2, 10 10 3)');
-- Answer:         LINESTRING(-10 10, 10 10)

SELECT ST_Force2D('POLYGON((2 2 2, 10 0 1, 10 5 1, 0 5 2, 2 2 2))');
-- Answer:         POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))

-- Also works on Geometries of mixed dimension:
SELECT ST_Force2D('LINESTRING(-10 10, 10 10 3)');
-- Answer:         LINESTRING(-10 10, 10 10)
{% endhighlight %}

##### See also

* [`ST_Force3D`](../ST_Force3D)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_Force2D.java" target="_blank">Source code</a>
