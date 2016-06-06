---
layout: docs
title: ST_MPointFromText
category: geom2D/geometry-conversion
is_function: true
description: Well Known Text &rarr; <code>MULTIPOINT</code>
prev_section: ST_MLineFromText
next_section: ST_MPolyFromText
permalink: /docs/1.3/ST_MPointFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_MPointFromText(VARCHAR wkt);
GEOMETRY ST_MPointFromText(VARCHAR wkt, INT srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='MULTIPOINT' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_MPointFromText('MULTIPOINT(4 2, 3 7, 6 8)');
-- Answer: MULTIPOINT((4 2), (3 7), (6 8))

SELECT ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 20 3)', 2154);
-- Answer: MULTIPOINT((5 5), (1 2), (3 4), (20 3))

SELECT ST_MPointFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a MULTIPOINT.
{% endhighlight %}

##### See also

* [`ST_PointFromText`](../ST_PointFromText), [`ST_MLineFromText`](../ST_MLineFromText), [`ST_MPolyFromText`](../ST_MPolyFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_MPointFromText.java" target="_blank">Source code</a>
