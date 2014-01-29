---
layout: docs
title: ST_LineFromText
category: h2spatial/geometry-conversion
description: Well Known Text &rarr; <code>LINESTRING</code>
prev_section: ST_GeomFromText
next_section: ST_LineFromWKB
permalink: /docs/dev/ST_LineFromText/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_LineFromText(varchar WKT, int srid);
{% endhighlight %}

### Description

Converts a Well Know Text `WKT` String into a `LINESTRING`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_LineFromText('LINESTRING EMPTY', 2154);
-- Answer: LINESTRING EMPTY

SELECT ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)', 2154);
-- Answer: LINESTRING(5 5, 1 2, 3 4, 99 3)

SELECT ST_LineFromText(
    'LINESTRING(0 18, 10 21, 16 23, 28 26, 44 31 )', 101);
-- Answer: LINESTRING(0 18, 10 21, 16 23, 28 26, 44 31)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_LineFromText.java" target="_blank">Source code</a>
