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

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax)
* [Source code](https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_ZMin.java)
* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
