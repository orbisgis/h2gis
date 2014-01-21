---
layout: docs
title: ST_ZMin
category: h2spatial-ext/properties
prev_section: ST_ZMax
next_section:
permalink: /docs/dev/ST_ZMin/
---

### Signature

{% highlight mysql %}
POINT ST_ZMin(Geometry geom);
{% endhighlight %}

### Description

Returns the minimum z-value of the given geometry.

### Example

{% highlight mysql %}
SELECT ST_ZMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    3.0
{% endhighlight %}

<img class="displayed" src="../ST_ZMin.png"/>

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_ZMin.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/28" target="_blank">#28</a>
