---
layout: docs
title: ST_LineFromWKB
category: h2spatial/geometry-conversion
description: Well Known Binary &rarr; <code>LINESTRING</code>
prev_section: ST_LineFromText
next_section: ST_MLineFromText
permalink: /docs/dev/ST_LineFromWKB/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_LineFromWKB(binary bytes, int srid);
{% endhighlight %}

### Description

Converts a Well Known Binary into a `LINESTRING`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_LineFromWKB('0000000002000000044014000000000000401400
    00000000003ff0000000000000400000000000000040080000000000004
    0100000000000004058c000000000004008000000000000', 4326);
-- Answer: LINESTRING (5 5, 1 2, 3 4, 99 3)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_LineFromWKB.java" target="_blank">Source code</a>
