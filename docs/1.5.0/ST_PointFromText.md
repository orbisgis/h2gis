---
layout: docs
title: ST_PointFromText
category: geom2D/geometry-conversion
is_function: true
description: Well Known Text &rarr; <code>POINT</code>
prev_section: ST_OSMMapLink
next_section: ST_PointFromWKB
permalink: /docs/1.5.0/ST_PointFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PointFromText(VARCHAR wkt);
GEOMETRY ST_PointFromText(VARCHAR wkt, INT srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='POINT' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_PointFromText('POINT(25 89)');
-- Answer: POINT(25 89)

SELECT ST_PointFromText('POINT(44 31)', 101);
-- Answer: POINT(44 31)

SELECT ST_PointFromText('MULTIPOINT((2 3), (4 5))', 2154);
-- Answer: The provided WKT Geometry is not a POINT.
{% endhighlight %}

##### See also

* [`ST_MPointFromText`](../ST_MPointFromText), [`ST_LineFromText`](../ST_LineFromText), [`ST_PolyFromText`](../ST_PolyFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_PointFromText.java" target="_blank">Source code</a>
