---
layout: docs
title: ST_PrecisionReducer
category: h2spatial-ext/process-geometries
description: Reduce the Geometry precision
prev_section: ST_Polygonize
next_section: ST_Simplify
permalink: /docs/dev/ST_PrecisionReducer/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PrecisionReducer(GEOMETRY geom, INT nbDec);
{% endhighlight %}

### Description
Reduces the Geometry precision. `nbDec` is the number of decimals to keep.

### Examples

{% highlight mysql %}
SELECT ST_PrecisionReducer('MULTIPOINT((190.1239999997 300),
                                       (10 11.1233))', 3);
-- Answer: MULTIPOINT((190.124 300), (10 11.123))

{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/processing/ST_PrecisionReducer.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>

