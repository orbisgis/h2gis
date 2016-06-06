---
layout: docs
title: ST_PolyFromWKB
category: geom2D/geometry-conversion
is_function: true
description: Well Known Binary &rarr; <code>POLYGON</code>
prev_section: ST_PolyFromText
next_section: ST_ToMultiLine
permalink: /docs/1.2/ST_PolyFromWKB/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PolyFromWKB(binary wkb);
GEOMETRY ST_PolyFromWKB(binary wkb, INT srid);
{% endhighlight %}

### Description

{% include from-wkb-desc.html type='POLYGON' %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_PolyFromWKB('000000000300000001000000064048800000000000403e0000000000004049000000000000403c000000000000404a800000000000403c000000000000404a8000000000004040000000000000404900000000000040400000000000004048800000000000403e000000000000');
-- Answer: POLYGON ((49 30, 50 28, 53 28, 53 32, 50 32, 49 30)) 

SELECT ST_PolyFromWKB('0020000003000010e600000001000000050000000000000000000000000000000000000000000000003ff00000000000003ff00000000000003ff00000000000003ff0000000000000000000000000000000000000000000000000000000000000', 2154);
-- Answer: POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))
{% endhighlight %}

##### See also

* [`ST_PointFromWKB`](../ST_PointFromWKB), [`ST_LineFromWKB`](../ST_LineFromWKB)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_PolyFromWKB.java" target="_blank">Source code</a>
