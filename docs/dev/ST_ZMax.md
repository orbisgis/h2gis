---
layout: docs
title: ST_ZMax
category: h2spatial-ext/properties
prev_section: ST_YMin
next_section: ST_ZMin
permalink: /docs/dev/ST_ZMax/
---

### Signature

{% highlight mysql %}
POINT ST_ZMax(Geometry geom);
{% endhighlight %}

### Description

Returns the maximum z-value of the given geometry.

### Example

{% highlight mysql %}
SELECT ST_ZMax('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    6.0
{% endhighlight %}

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMin`](../ST_ZMin)
* [Source code](https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_ZMax.java)
* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
