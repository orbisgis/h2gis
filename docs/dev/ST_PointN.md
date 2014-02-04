---
layout: docs
title: ST_PointN
category: h2spatial/properties
description: Return the i POINT of a <code>LINESTRING</code>
prev_section: ST_NumPoints
next_section: ST_PointOnSurface
permalink: /docs/dev/ST_PointN/
---

### Signature

{% highlight mysql %}
POINT ST_PointN(Geometry geometry,integer i);
{% endhighlight %}

### Description

Returns the `POINT` number `i` of a `LINESTRING` or Null if the input parameter is not a `LINESTRING`.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_PointN('LINESTRING (1 1, 1 6, 2 2, -1 2))', 2);
-- Answer: POINT (1 6)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_PointN.java" target="_blank">Source code</a>
