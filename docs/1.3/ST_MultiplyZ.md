---
layout: docs
title: ST_MultiplyZ
category: geom3D/edit-geometries
is_function: true
description: Return a Geometry's <i>z</i>-values by a factor
prev_section: ST_Interpolate3DLine
next_section: ST_Reverse3DLine
permalink: /docs/1.3/ST_MultiplyZ/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_MultiplyZ(GEOMETRY geom, DOUBLE zFactor);
{% endhighlight %}

### Description

Multiply the *z*-value of each coordinate of `geom` by a `zFactor`.
Non-existent *z*-values are not updated.

### Examples

{% highlight mysql %}
SELECT ST_MultiplyZ('MULTIPOINT((190 300 1), (10 11 50))', 10);
-- Answer:           MULTIPOINT((190 300 10), (10 11 500))

-- Non-existent z-values are not updated:
SELECT ST_MultiplyZ('MULTIPOINT((190 300), (10 11))', 10);
-- Answer:           MULTIPOINT((190 300), (10 11)
SELECT ST_MultiplyZ('MULTIPOINT((190 300 10), (10 11))', 10);
-- Answer:           MULTIPOINT((190 300 100), (10 11))
{% endhighlight %}

##### See also

* [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_MultiplyZ.java" target="_blank">Source code</a>
