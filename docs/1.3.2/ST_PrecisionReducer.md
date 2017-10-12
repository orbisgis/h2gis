---
layout: docs
title: ST_PrecisionReducer
category: geom2D/process-geometries
is_function: true
description: Reduce a Geometry's precision
prev_section: ST_Polygonize
next_section: ST_RingSideBuffer
permalink: /docs/1.3.2/ST_PrecisionReducer/
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

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/generalize/ST_PrecisionReducer.java" target="_blank">Source code</a>
* JTS [GeometryPrecisionReducer#reduce][jts]

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/precision/GeometryPrecisionReducer.html#reduce(com.vividsolutions.jts.geom.Geometry)
