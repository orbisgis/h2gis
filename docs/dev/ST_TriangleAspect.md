---
layout: docs
title: ST_TriangleAspect
category: geom3D/topography
is_function: true
description: Return the aspect of a triangle
prev_section: geom3D/topography
next_section: ST_TriangleContouring
permalink: /docs/dev/ST_TriangleAspect/
---

### Signature

{% highlight mysql %}
double ST_TriangleAspect(GEOMETRY geom);
{% endhighlight %}

### Description
Returns the aspect value of steepest downhill slope for a triangle. 
The aspect value is expressed in degrees compared to the north direction.

### Examples

{% highlight mysql %}
SELECT ST_TriangleAspect('POLYGON ((0 0 0, 2 0 0, 1 1 0, 0 0 0))');
-- Answer: 0.0

SELECT ST_TriangleAspect('POLYGON ((0 0 1, 10 0 0, 0 10 1, 0 0 1))');
-- Answer: 90.0
{% endhighlight %}

<img class="displayed" src="../ST_TriangleAspect_1.png"/>

##### See also

* [`ST_TriangleContouring`](../ST_TriangleContouring),
[`ST_TriangleDirection`](../ST_TriangleDirection),[`ST_TriangleSlope`](../ST_TriangleSlope)
* <a href="https://github.com/irstv/H2GIS/blob/51910b27b5dc2b3b4353bb43a683f8649628ea8d/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/topography/ST_TriangleAspect.java" target="_blank">Source code</a>

