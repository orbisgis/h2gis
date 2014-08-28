---
layout: docs
title: ST_TriangleDirection
category: geom3D/topography
is_function: true
description: Compute the main slope direction on a triangle
prev_section: ST_TriangleContouring
next_section: ST_TriangleSlope
permalink: /docs/dev/ST_TriangleDirection/
---

### Signature

{% highlight mysql %}
LINESTRING ST_TriangleDirection(GEOMETRY geom);
{% endhighlight %}

### Description
Computes the main slope direction on a triangle and represent it as a
LINESTRING.

### Examples

{% highlight mysql %}
SELECT ST_TriangleDirection('POLYGON((0 0 0, 2 0 0, 1 1 0, 0 0 0))');
-- Answer: LINESTRING EMPTY

SELECT ST_TriangleDirection('POLYGON((0 0 0, 4 0 0, 2 3 6, 0 0 0))');
-- Answer: LINESTRING(2 1 2, 2 0 0)
{% endhighlight %}

<img class="displayed" src="../ST_TriangleDirection_1.png"/>

##### See also

* [`ST_TriangleAspect`](../ST_TriangleAspect),
[`ST_TriangleContouring`](../ST_TriangleContouring), [`ST_TriangleSlope`](../ST_TriangleSlope)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/topography/ST_TriangleDirection.java" target="_blank">Source code</a>
