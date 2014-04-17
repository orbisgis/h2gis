---
layout: docs
title: ST_YMin
category: Geometry2D/properties
description: Return the minimum y-value of a Geometry
prev_section: ST_YMax
next_section: Geometry3D/list-function-3d
permalink: /docs/dev/ST_YMin/
---

### Signature

{% highlight mysql %}
double ST_YMin(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the minimum y-value of `geom`.

### Examples

{% highlight mysql %}
SELECT ST_YMin('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    2.0
{% endhighlight %}

<img class="displayed" src="../ST_YMin.png"/>

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_YMin.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/28" target="_blank">#28</a>
