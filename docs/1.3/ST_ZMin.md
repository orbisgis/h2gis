---
layout: docs
title: ST_ZMin
category: geom3D/properties
is_function: true
description: Return the minimum z-value of a Geometry
prev_section: ST_ZMax
next_section: geom3D/topography
permalink: /docs/1.3/ST_ZMin/
---

### Signature

{% highlight mysql %}
DOUBLE ST_ZMin(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the minimum z-value of `geom`.

### Example

{% highlight mysql %}
SELECT ST_ZMin('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    3.0
{% endhighlight %}

<img class="displayed" src="../ST_ZMin.png"/>

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax)
* <a href="https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_ZMin.java" target="_blank">Source code</a>

