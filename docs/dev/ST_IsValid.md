---
layout: docs
title: ST_IsValid
category: Geometry2D/properties
description: Return true if the Geometry is valid
prev_section: ST_IsSimple
next_section: ST_IsValidDetail
permalink: /docs/dev/ST_IsValid/
---

### Signature

{% highlight mysql %}
boolean ST_IsValid(GEOMETRY geom);
{% endhighlight %}

### Description

Returns true if `geom` is valid.

### Examples

{% highlight mysql %}
SELECT ST_IsValid('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))');
-- Answer:    true

SELECT ST_IsValid('POLYGON ((0 0, 10 0, 10 5, 6 -2, 0 0))');
-- Answer:    false
{% endhighlight %}

<img class="displayed" src="../ST_IsValid.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/predicates/ST_IsValid.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/26" target="_blank">#26</a>
