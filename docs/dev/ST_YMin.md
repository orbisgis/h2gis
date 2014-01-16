---
layout: docs
title: ST_YMin
category: h2spatial-ext/properties
prev_section: ST_YMax
next_section: ST_ZMax
permalink: /docs/dev/ST_YMin/
---

### Signature

{% highlight mysql %}
POINT ST_YMin(Geometry geom);
{% endhighlight %}

### Description

Returns the minimum y-value of the given geometry.

### Examples

{% highlight mysql %}
SELECT ST_YMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    2.0
{% endhighlight %}

<img class="displayed" src="../ST_YMin.png"/>

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_YMin.java" target="_blank">Source code</a>
* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
