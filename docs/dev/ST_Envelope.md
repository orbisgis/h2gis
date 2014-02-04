---
layout: docs
title: ST_Envelope
category: h2spatial/properties
description: Return Geometry envelope as <code>GEOMETRY</code>
prev_section: ST_EndPoint
next_section: ST_ExteriorRing
permalink: /docs/dev/ST_Envelope/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Envelope(GEOMETRY geom, int srid);
GEOMETRY ST_Envelope(GEOMETRYCOLLECTION geom, int srid);
{% endhighlight %}

### Description

Returns Geometry envelope as `GEOMETRY`

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Envelope('POINT(1 2)', 2154);
-- Answer: POINT (1 2)

SELECT ST_Envelope('MULTIPOINT(1 2, 3 1, 2 2, 5 1, 1 -1)', 2154);
-- Answer: POLYGON((1 -1, 1 2, 5 2, 5 -1, 1 -1))
{% endhighlight %}

<img class="displayed" src="../ST_Envelope_1.png"/>

{% highlight mysql %}
SELECT ST_Envelope('LINESTRING(1 1, 2 1)', 2154);
-- Answer: LINESTRING (1 1, 2 1)

SELECT ST_Envelope('LINESTRING(1 2, 5 3, 2 6)', 2154);
-- Answer: POLYGON((1 2, 1 6, 5 6, 5 2, 1 2))
{% endhighlight %}

<img class="displayed" src="../ST_Envelope_2.png"/>

{% highlight mysql %}
SELECT ST_Envelope('POLYGON ((0 -1, 0 2, 3 2, 3 -1, 0 -1))', 
    2154);
-- Answer: POLYGON ((0 -1, 0 2, 3 2, 3 -1, 0 -1))
{% endhighlight %}

##### Comparison with [`ST_Extent`](../ST_Extent)

{% include extent-envelope-cf.html %}

##### See also

* [`ST_Extent`](../ST_Extent)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Envelope.java" target="_blank">Source code</a>
