---
layout: docs
title: ST_ExteriorRing
category: geom2D/properties
is_function: true
description: Return the exterior ring of a <code>POLYGON</code>
prev_section: ST_Extent
next_section: ST_GeometryN
permalink: /docs/1.3.2/ST_ExteriorRing/
---

### Signature

{% highlight mysql %}
LINEARRING ST_ExteriorRing(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the exterior ring of `geom` as a `LINEARRING`, or `NULL` if `geom` is
not a `POLYGON`.

{% include type-warning.html type='MULTIPOLYGON' %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_ExteriorRing('POLYGON((0 -1, 0 2, 3 2, 3 -1, 0 -1))');
-- Answer: LINEARRING(0 -1, 0 2, 3 2, 3 -1, 0 -1)
{% endhighlight %}

<img class="displayed" src="../ST_ExteriorRing_1.png"/>

{% highlight mysql %}
SELECT ST_ExteriorRing('MULTIPOLYGON(((0 0, 10 0, 5 5, 0 0)),
                                      ((10 0, 5 5, 10 10, 10 0)))');
-- Answer: NULL

SELECT ST_ExteriorRing('POINT(1 2)');
-- Answer: NULL
{% endhighlight %}

##### Comparison with [`ST_InteriorRingN`](../ST_InteriorRingN)

{% include exteriorring-interiorringn-cf.html %}

##### Comparison with [`ST_Boundary`](../ST_Boundary)

{% include exteriorring-boundary-cf.html %}

##### See also

* [`ST_Boundary`](../ST_Boundary), [`ST_InteriorRingN`](../ST_InteriorRingN)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_ExteriorRing.java" target="_blank">Source code</a>
