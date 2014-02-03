---
layout: docs
title: ST_Boundary
category: h2spatial/properties
description: Return Geometry boundary as <code>GEOMETRY</code>
prev_section: ST_Area
next_section: ST_Centroid
permalink: /docs/dev/ST_Boundary/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Boundary(GEOMETRY geom);
GEOMETRY ST_Boundary(GEOMETRY geom, int srid);
{% endhighlight %}

### Description

Returns boundary of a `GEOMETRY`.

<div class="note">

  <h8>ST_Boundary does not support <code>GEOMETRYCOLLECTION</code> arguments.</h8>

</div>

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Boundary('POINT(2 2)');
-- Answer: GEOMETRYCOLLECTION EMPTY

SELECT ST_Boundary('LINESTRING(4 6, 1 1, 5 5)');
-- Answer: MULTIPOINT((4 6), (5 5))
{% endhighlight %}

<img class="displayed" src="../ST_Boundary_1.png"/>

{% highlight mysql %}
SELECT ST_Boundary('POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))', 2154);
-- Answer: LINEARRING(0 0, 10 0, 10 10, 0 10, 0 0)
{% endhighlight %}

<img class="displayed" src="../ST_Boundary_2.png"/>

{% highlight mysql %}
SELECT ST_Boundary('POLYGON((5 4, 1 1, 3 4, 4 5, 5 4))');
-- Answer: LINEARRING(5 4, 1 1, 3 4, 4 5, 5 4)
{% endhighlight %}

##### Comparison with [`ST_ExteriorRing`](../ST_ExteriorRing)

{% include exteriorring-boundary-cf.html %}

##### See also

* [`ST_ExteriorRing`](../ST_ExteriorRing)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Boundary.java" target="_blank">Source code</a>
