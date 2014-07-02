---
layout: docs
title: ST_MLineFromText
category: geom2D/geometry-conversion
is_function: true
description: Well Known Text &rarr; <code>MULTILINESTRING</code>
prev_section: ST_LineFromWKB
next_section: ST_MPointFromText
permalink: /docs/dev/ST_MLineFromText/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_MLineFromText(VARCHAR wkt, INT srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='MULTILINESTRING' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_MLineFromText('MULTILINESTRING((10 48, 10 21, 10 0),
                                         (16 0, 16 23, 16 48))', 101);
-- Answer: MULTILINESTRING((10 48, 10 21, 10 0),
--                         (16 0, 16 23, 16 48))

SELECT ST_MLineFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a MULTILINESTRING.
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_MLineFromText.java" target="_blank">Source code</a>
