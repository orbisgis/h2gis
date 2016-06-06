---
layout: docs
title: ST_XMax
category: geom2D/properties
is_function: true
description: Return the maximum x-value of a Geometry
prev_section: ST_X
next_section: ST_XMin
permalink: /docs/1.3/ST_XMax/
---

### Signature

{% highlight mysql %}
DOUBLE ST_XMax(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the maximum x-value of `geom`.

### Example

{% highlight mysql %}
SELECT ST_XMax('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    4.0
{% endhighlight %}

<img class="displayed" src="../ST_XMax.png"/>

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_XMax.java" target="_blank">Source code</a>
