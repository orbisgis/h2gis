---
layout: docs
title: ST_YMin
prev_section: h2spatial-ext/ST_YMax
next_section: h2spatial-ext/ST_ZMax
permalink: /docs/dev/h2spatial-ext/ST_YMin/
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

![warning](../ST_YMin.png)

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* [Source code](https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_YMin.java)
* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
