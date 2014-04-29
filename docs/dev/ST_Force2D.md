---
layout: docs
title: ST_Force2D
category: Geometry2D/geometry-conversion
description: Convert a XYZ geometry to XY
prev_section: ST_AsWKT
next_section: ST_GeomFromText
permalink: /docs/dev/ST_Force2D/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_Force2D(GEOMETRY geom);
{% endhighlight %}

### Description
Returns a `GEOMETRY` in 2-dimensional. The output `GEOMETRY` will only have the X and Y coordinates.

### Examples

{% highlight mysql %}
SELECT ST_Force2D('POINT(-10 10)');
-- Answer: POINT(-10 10)

SELECT ST_Force2D('POINT(-10 10 6)');
-- Answer: POINT(-10 10)

SELECT ST_Force2D('LINESTRING(-10 10 2, 10 10 3)');
-- Answer: LINESTRING(-10 10, 10 10)

SELECT ST_Force2D('POLYGON((2 2 2, 10 0 1, 10 5 1, 0 5 2, 2 2 2))');
-- Answer: POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))
{% endhighlight %}

##### See also

* [`ST_Force3D`](../ST_Force3D)
* <a href="https://github.com/irstv/H2GIS/blob/51910b27b5dc2b3b4353bb43a683f8649628ea8d/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/convert/ST_Force2D.java" target="_blank">Source code</a>

