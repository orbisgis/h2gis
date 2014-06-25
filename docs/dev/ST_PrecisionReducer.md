---
layout: docs
title: ST_PrecisionReducer
category: h2spatial-ext/process-geometries
description: Reduce a Geometry's precision
prev_section: ST_Polygonize
next_section: ST_Simplify
permalink: /docs/dev/ST_PrecisionReducer/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PrecisionReducer(GEOMETRY geom, INT n);
{% endhighlight %}

### Description

Reduces the precision of `geom` to `n` decimal places.

### Examples

{% highlight mysql %}
SELECT ST_PrecisionReducer(
            'MULTIPOINT((190.1239999997 300), (10 11.1233))', 3);
-- Answer:   MULTIPOINT((190.124 300), (10 11.123))

{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/processing/ST_PrecisionReducer.java" target="_blank">Source code</a>
* JTS [GeometryPrecisionReducer#reduce][jts]
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/precision/GeometryPrecisionReducer.html#reduce(com.vividsolutions.jts.geom.Geometry)
