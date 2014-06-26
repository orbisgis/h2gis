---
layout: docs
title: ST_PointFromText
category: geom2D/geometry-conversion
is_function: true
description: Well Known Text &rarr; <code>POINT</code>
prev_section: ST_MPolyFromText
next_section: ST_PolyFromText
permalink: /docs/dev/ST_PointFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PointFromText(varchar wkt, int srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='POINT' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_PointFromText('POINT(44 31)', 101);
-- Answer: POINT(44 31)

SELECT ST_PointFromText('MULTIPOINT((2 3), (4 5))', 2154);
-- Answer: The provided WKT Geometry is not a POINT.
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_PointFromText.java" target="_blank">Source code</a>
