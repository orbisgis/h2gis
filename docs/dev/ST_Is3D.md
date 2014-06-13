---
layout: docs
title: ST_Is3D
category: Geometry3D/properties
description: Return 1 if a Geometry has a z-coordinate, otherwise 0.
prev_section: Geometry3D/properties
next_section: ST_Z
permalink: /docs/dev/ST_Is3D/
---

### Signature

{% highlight mysql %}
int ST_Is3D(GEOMETRY geom);
{% endhighlight %}

### Description

Returns 1 if a `geom` has a z-coordinate, otherwise 0.

### Examples

{% highlight mysql %}
SELECT ST_Is3D('LINESTRING(1 1, 2 1, 2 2, 1 2, 1 1)'::geometry);
-- Answer: 0

SELECT ST_Is3D('LINESTRING(1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)'
                ::geometry);
-- Answer: 1
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/ce387709832710e8a2932c3be3c2d0535a3fdf71/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Is3D.java" target="_blank">Source code</a>
