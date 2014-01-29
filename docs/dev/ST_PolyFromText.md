---
layout: docs
title: ST_PolyFromText
category: h2spatial/geometry-conversion
description: Well Known Text &rarr; <code>POLYGON</code>
prev_section: ST_PointFromText
next_section: ST_PolyFromWKB
permalink: /docs/dev/ST_PolyFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PolyFromText(varchar WKT, int srid);
{% endhighlight %}

### Description

Converts a Well Known Text `WKT` String into a `POLYGON`.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_PolyFromText(
    'POLYGON((50 31, 54 31, 54 29, 50 29, 50 31))', 2154);
-- Answer: POLYGON((50 31, 54 31, 54 29, 50 29, 50 31))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_PolyFromText.java" target="_blank">Source code</a>
