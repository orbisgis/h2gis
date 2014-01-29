---
layout: docs
title: ST_PointFromText
category: h2spatial/geometry-conversion
description: Well Known Text &rarr; <code>POINT</code>
prev_section: ST_MPolyFromText
next_section: ST_PolyFromText
permalink: /docs/dev/ST_PointFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PointFromText(varchar WKT, int srid);
{% endhighlight %}

### Description

Converts a Well Known Text `WKT` String into a `POINT`.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_PointFromText('POINT(44 31)', 101);
-- Answer: POINT(44 31)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_PointFromText.java" target="_blank">Source code</a>
