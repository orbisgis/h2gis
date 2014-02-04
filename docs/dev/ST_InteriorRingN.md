---
layout: docs
title: ST_InteriorRingN
category: h2spatial/properties
description: Return the interior ring of a `POLYGON`
prev_section: ST_GeometryType
next_section: ST_IsClosed
permalink: /docs/dev/ST_InteriorRingN/
---

### Signature

{% highlight mysql %}
LINESTRING ST_InteriorRing(GEOMETRY geom,integer i);
{% endhighlight %}

### Description

Returns the hole number`i` of a `POLYGON` or Null if parameter is not a `POLYGON`.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_InteriorRingN('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0), 
                         (1 1, 2 1, 2 5, 1 5, 1 1), 
                         (8 5, 8 4, 9 4, 9 5, 8 5))',2);
-- Answer: LINEARRING(8 5, 8 4, 9 4, 9 5, 8 5)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_InteriorRingN.java" target="_blank">Source code</a>
