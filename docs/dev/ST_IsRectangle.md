---
layout: docs
title: ST_IsRectangle
category: h2spatial-ext/predicates
description: Return true if the given geometry is a rectangle
prev_section: ST_DWithin
next_section: ST_IsValid
permalink: /docs/dev/ST_IsRectangle/
---

### Signature

{% highlight mysql %}
boolean ST_IsRectangle(GEOMETRY geom);
{% endhighlight %}

### Description

Returns true if `geom` is a rectangle.

### Examples

{% highlight mysql %}
SELECT ST_IsRectangle('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))'::Geometry);
-- Answer:    true

SELECT ST_IsRectangle('POLYGON ((0 0, 10 0, 10 7, 0 5, 0 0))'::Geometry);
-- Answer:    false
{% endhighlight %}

<img class="displayed" src="../ST_IsRectangle.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/predicates/ST_IsRectangle.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/26" target="_blank">#26</a>
