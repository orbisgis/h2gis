---
layout: docs
title: ST_AddZ
category: h2spatial-ext/edit-geometries
description: Return a Geometry with the z value updated
prev_section: ST_AddPoint
next_section: ST_Densify
permalink: /docs/dev/ST_AddZ/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_AddZ(GEOMETRY geom, DOUBLE z);
{% endhighlight %}

### Description
Returns a `GEOMETRY` where the output z value is the sum to the `z` value and the input z value of each vertex.
NaN values are not updated.

### Examples

{% highlight mysql %}
SELECT ST_AddZ('MULTIPOINT((190 300 1), (10 11))', 10);
-- Answer: MULTIPOINT((190 300 11), (10 11))
SELECT ST_Z(ST_GeometryN(ST_AddZ('MULTIPOINT((190 300 1),
                                              (10 11))',
                                  10), 2));
-- Answer: NaN

SELECT ST_AddZ('MULTIPOINT((190 300 10), (10 11 5))', -10)
-- Answer: MULTIPOINT((190 300 0), (10 11 -5))

SELECT ST_AddZ('POLYGON((1 1 5, 1 7 10, 7 7 -1, 7 1 -1, 1 1 5))',
               -10);
-- Answer: POLYGON((1 1 -5, 1 7 0, 7 7 -11, 7 1 -11, 1 1 -5))
{% endhighlight %}

##### See also

* [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_AddZ.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
