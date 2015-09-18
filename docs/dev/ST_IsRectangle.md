---
layout: docs
title: ST_IsRectangle
category: geom2D/properties
is_function: true
description: Return true if the Geometry is a rectangle
prev_section: ST_IsEmpty
next_section: ST_IsRing
permalink: /docs/dev/ST_IsRectangle/
---

### Signature

{% highlight mysql %}
BOOLEAN ST_IsRectangle(GEOMETRY geom);
{% endhighlight %}

### Description

Returns true if `geom` is a rectangle.

### Examples

{% highlight mysql %}
SELECT ST_IsRectangle('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))');
-- Answer:    TRUE

SELECT ST_IsRectangle('POLYGON((0 0, 10 0, 10 7, 0 5, 0 0))');
-- Answer:    FALSE
{% endhighlight %}

<img class="displayed" src="../ST_IsRectangle.png"/>

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/predicates/ST_IsRectangle.java" target="_blank">Source code</a>
