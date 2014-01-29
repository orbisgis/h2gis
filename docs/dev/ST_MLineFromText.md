---
layout: docs
title: ST_MLineFromText
category: h2spatial/geometry-conversion
description: Well Known Text &rarr; <code>MULTILINESTRING</code>
prev_section: ST_LineFromWKB
next_section: ST_MPointFromText
permalink: /docs/dev/ST_MLineFromText/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_MLineFromText(varchar WKT, int srid);
{% endhighlight %}

### Description

Converts a Well Known Text `WKT` String into a `MULTILINESTRING`.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_MLineFromText('MULTILINESTRING((10 48,10 21,10 0), 
    (16 0,16 23,16 48))', 101);
-- Answer: MULTILINESTRING((10 48, 10 21, 10 0), 
--  (16 0, 16 23, 16 48))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_MLineFromText.java" target="_blank">Source code</a>
