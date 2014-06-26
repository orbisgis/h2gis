---
layout: docs
title: ST_MPointFromText
category: geom2D/geometry-conversion
description: Well Known Text &rarr; <code>MULTIPOINT</code>
prev_section: ST_MLineFromText
next_section: ST_MPolyFromText
permalink: /docs/dev/ST_MPointFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_MPointFromText(varchar wkt, int srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='MULTIPOINT' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 20 3)', 2154);
-- Answer: MULTIPOINT((5 5), (1 2), (3 4), (20 3))

SELECT ST_MPointFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a MULTIPOINT.
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_MPointFromText.java" target="_blank">Source code</a>
