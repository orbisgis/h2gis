---
layout: docs
title: ST_LineFromText
category: Geometry2D/geometry-conversion
description: Well Known Text &rarr; <code>LINESTRING</code>
prev_section: ST_GeomFromText
next_section: ST_LineFromWKB
permalink: /docs/dev/ST_LineFromText/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_LineFromText(varchar wkt, int srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='LINESTRING' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)', 2154);
-- Answer: LINESTRING(5 5, 1 2, 3 4, 99 3)

SELECT ST_LineFromText('LINESTRING(0 0 -1, 2 0 2, 2 1 3)', 2154);
-- Answer: LINESTRING(0 0, 2 0, 2 1)

SELECT ST_LineFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a LINESTRING.
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_LineFromText.java" target="_blank">Source code</a>
