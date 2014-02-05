---
layout: docs
title: ST_ExteriorRing
category: h2spatial/properties
description: <code>POLYGON</code> &rarr; <code>LinearRing</code>
prev_section: ST_Envelope
next_section: ST_GeometryN
permalink: /docs/dev/ST_ExteriorRing/
---

### Signature

{% highlight mysql %}
LinearRing ST_ExteriorRing(GEOMETRY geom);
{% endhighlight %}

### Description

Returns a `LinearRing` instance or Null if parameter is not a `POLYGON`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_ExteriorRing('POLYGON((0 -1, 0 2, 3 2, 3 -1, 0 -1))');
-- Answer: LINEARRING(0 -1, 0 2, 3 2, 3 -1, 0 -1)
{% endhighlight %}

<img class="displayed" src="../ST_ExteriorRing_1.png"/>

##### Comparison with [`ST_Boundary`](../ST_Boundary)

{% include exteriorring-boundary-cf.html %}

##### See also

* [`ST_Boundary`](../ST_Boundary), [`ST_InteriorRingN`](../ST_InteriorRingN)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_ExteriorRing.java" target="_blank">Source code</a>
