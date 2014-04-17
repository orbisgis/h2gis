---
layout: docs
title: ST_ZMax
category: Geometry3D/properties
description: Return the maximum z-value of a Geometry
prev_section: ST_YMin
next_section: ST_ZMin
permalink: /docs/dev/ST_ZMax/
---

### Signature

{% highlight mysql %}
double ST_ZMax(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the maximum z-value of `geom`.

### Example

{% highlight mysql %}
SELECT ST_ZMax('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    6.0
{% endhighlight %}

<img class="displayed" src="../ST_ZMax.png"/>

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_ZMax.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/28" target="_blank">#28</a>
