---
layout: docs
title: ST_PolyFromWKB
category: h2spatial/geometry-conversion
description: Well Known Binary &rarr; <code>POLYGON</code>
prev_section: ST_PolyFromText
next_section: h2spatial/operators
permalink: /docs/dev/ST_PolyFromWKB/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_PolyFromWKB(binary wkb, int srid);
{% endhighlight %}

### Description

{% include from-wkb-desc.html type='POLYGON' %}
{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_PolyFromWKB('0020000003000010e600000001000000050000000000000000000000000000000000000000000000003ff00000000000003ff00000000000003ff00000000000003ff0000000000000000000000000000000000000000000000000000000000000', 2154);
-- Answer:  'POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_PolyFromWKB.java" target="_blank">Source code</a>
