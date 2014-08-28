---
layout: docs
title: ST_TriangleSlope
category: geom3D/topography
is_function: true
description: Compute the slope direction of a triangle
prev_section: ST_TriangleDirection
next_section: geom3D/triangulation
permalink: /docs/dev/ST_TriangleSlope/
---

### Signature

{% highlight mysql %}
DOUBLE ST_TriangleSlope(GEOMETRY geom);
{% endhighlight %}

### Description
Computes the slope direction of a triangle expressed in percents.

<img class="displayed" src="../ST_TriangleSlope_0.png"/>

### Examples

{% highlight mysql %}
SELECT ST_TriangleSlope('POLYGON((0 0 0, 2 0 0, 1 1 0, 0 0 0))');
-- Answer: 0

SELECT ST_TriangleSlope('POLYGON((0 0 10, 10 0 1, 5 5 10, 0 0 10))');
-- Answer: 127.27922061357853

SELECT ST_TriangleSlope('POLYGON((0 0 0, 4 0 0, 2 3 6, 0 0 0))');
-- Answer: 200.0
{% endhighlight %}

<img class="displayed" src="../ST_TriangleSlope_1.png"/>

##### See also

* [`ST_TriangleAspect`](../ST_TriangleAspect),
[`ST_TriangleContouring`](../ST_TriangleContouring), [`ST_TriangleDirection`](../ST_TriangleDirection)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/topography/ST_TriangleSlope.java" target="_blank">Source code</a>
