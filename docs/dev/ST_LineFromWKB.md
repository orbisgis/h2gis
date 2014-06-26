---
layout: docs
title: ST_LineFromWKB
category: geom2D/geometry-conversion
description: Well Known Binary &rarr; <code>LINESTRING</code>
prev_section: ST_LineFromText
next_section: ST_MLineFromText
permalink: /docs/dev/ST_LineFromWKB/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_LineFromWKB(binary wkb, int srid);
{% endhighlight %}

### Description

{% include from-wkb-desc.html type='LINESTRING' %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_LineFromWKB('000000000200000004401400000000000040140000000000003ff00000000000004000000000000000400800000000000040100000000000004058c000000000004008000000000000', 4326);
-- Answer: LINESTRING(5 5, 1 2, 3 4, 99 3)

SELECT ST_LineFromWKB(ST_AsBinary('POINT(2 3)'::Geometry), 2154);
-- Answer: Provided WKB is not a LINESTRING.
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_LineFromWKB.java" target="_blank">Source code</a>
