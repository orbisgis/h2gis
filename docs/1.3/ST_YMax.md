---
layout: docs
title: ST_YMax
category: geom2D/properties
is_function: true
description: Return the maximum y-value of a Geometry
prev_section: ST_Y
next_section: ST_YMin
permalink: /docs/1.3/ST_YMax/
---

### Signature

{% highlight mysql %}
DOUBLE ST_YMax(GEOMETRY geom);
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
* <a href="https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_YMax.java" target="_blank">Source code</a>
