---
layout: docs
title: ST_ZMin
category: geom3D/properties
description: Return the minimum z-value of a Geometry
prev_section: ST_ZMax
next_section: geom3D/topography
permalink: /docs/dev/ST_ZMin/
---

### Signature

{% highlight mysql %}
double ST_ZMin(GEOMETRY geom);
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
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_ZMin.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/28" target="_blank">#28</a>

