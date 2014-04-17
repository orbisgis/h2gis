---
layout: docs
title: ST_YMax
category: Geometry2D/properties
description: Return the maximum y-value of a Geometry
prev_section: ST_XMin
next_section: ST_YMin
permalink: /docs/dev/ST_YMax/
---

### Signature

{% highlight mysql %}
double ST_YMax(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the maximum y-value of `geom`.

### Example

{% highlight mysql %}
SELECT ST_YMax('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    5.0
{% endhighlight %}

<img class="displayed" src="../ST_YMax.png"/>

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_YMax.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/28" target="_blank">#28</a>
