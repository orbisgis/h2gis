---
layout: docs
title: ST_PolyFromText
category: geom2D/geometry-conversion
is_function: true
description: Well Known Text &rarr; <code>POLYGON</code>
prev_section: ST_PointFromWKB
next_section: ST_PolyFromWKB
permalink: /docs/1.3.2/ST_PolyFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PolyFromText(VARCHAR wkt);
GEOMETRY ST_PolyFromText(VARCHAR wkt, INT srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='POLYGON' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_PolyFromText('POLYGON ((49 30, 50 28, 53 28, 53 32, 50 32, 49 30))');
-- Answer: POLYGON ((49 30, 50 28, 53 28, 53 32, 50 32, 49 30))

SELECT ST_PolyFromText('POLYGON((50 31, 54 31, 54 29, 50 29, 50 31))', 2154);
-- Answer: POLYGON((50 31, 54 31, 54 29, 50 29, 50 31))

SELECT ST_PolyFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a POLYGON.
{% endhighlight %}

##### See also

* [`ST_MPolyFromText`](../ST_MPolyFromText), [`ST_PointFromText`](../ST_PointFromText), [`ST_LineFromText`](../ST_LineFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_PolyFromText.java" target="_blank">Source code</a>
