---
layout: docs
title: ST_XMin
category: h2spatial-ext/properties
prev_section: ST_XMax
next_section: ST_YMax
permalink: /docs/dev/ST_XMin/
---

### Signature

{% highlight mysql %}
POINT ST_XMin(Geometry geom);
{% endhighlight %}

### Description

Returns the minimum x-value of the given geometry.

### Example

{% highlight mysql %}
SELECT ST_XMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    1.0
{% endhighlight %}

<img class="displayed" src="../ST_XMin.png"/>

##### See also

* [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_XMin.java" target="_blank">Source code</a>
* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
