---
layout: docs
title: ST_LineFromText
category: geom2D/geometry-conversion
is_function: true
description: Well Known Text &rarr; <code>LINESTRING</code>
prev_section: ST_Holes
next_section: ST_LineFromWKB
permalink: /docs/dev/ST_LineFromText/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_LineFromText(VARCHAR wkt);
GEOMETRY ST_LineFromText(VARCHAR wkt, INT srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='LINESTRING' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_LineFromText('LINESTRING(2 3, 4 6, 10 6, 12 15)');
-- Answer: LINESTRING(2 3, 4 6, 10 6, 12 15)

SELECT ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)', 2154);
-- Answer: LINESTRING(5 5, 1 2, 3 4, 99 3)

SELECT ST_LineFromText('LINESTRING(0 0 -1, 2 0 2, 2 1 3)', 2154);
-- Answer: LINESTRING(0 0, 2 0, 2 1)

SELECT ST_LineFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a LINESTRING.
{% endhighlight %}

##### See also

* [`ST_MLineFromText`](../ST_MLineFromText), [`ST_PointFromText`](../ST_PointFromText), [`ST_PolyFromText`](../ST_PolyFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_LineFromText.java" target="_blank">Source code</a>
