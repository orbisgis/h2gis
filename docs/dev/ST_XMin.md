---
layout: docs
title: ST_XMin
category: geom2D/properties
is_function: true
description: Return the minimum x-value of a Geometry
prev_section: ST_XMax
next_section: ST_Y
permalink: /docs/dev/ST_XMin/
---

### Signature

{% highlight mysql %}
double ST_XMin(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the minimum x-value of `geom`.

### Example

{% highlight mysql %}
SELECT ST_XMin('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    1.0
{% endhighlight %}

<img class="displayed" src="../ST_XMin.png"/>

##### See also

* [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_XMin.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/28" target="_blank">#28</a>
