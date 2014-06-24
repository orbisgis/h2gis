---
layout: docs
title: ST_MultiplyZ
category: h2spatial-ext/edit-geometries
description: Return a Geometry with the z value is multiply
prev_section: ST_Interpolate3DLine
next_section: ST_Normalize
permalink: /docs/dev/ST_MultiplyZ/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_MultiplyZ(GEOMETRY geom, DOUBLE z);
{% endhighlight %}

### Description
Multiply the z value of each vertex of the Geometry by a `z` value
NaN values are not updated.

### Examples

{% highlight mysql %}
SELECT ST_MultiplyZ('MULTIPOINT((190 300), (10 11))', 10);
-- Answer: MULTIPOINT((190 300), (10 11)
SELECT ST_Z(ST_MultiplyZ('MULTIPOINT((190 300), (10 11))', 10));
-- Answer: NaN

SELECT ST_MultiplyZ('MULTIPOINT((190 300 1), (10 11 50))',
                    10);
-- Answer: MULTIPOINT((190 300 10), (10 11 500))

SELECT ST_MultiplyZ('MULTIPOINT((190 300 10), (10 11))', 10);
-- Answer: MULTIPOINT((190 300 100), (10 11))

SELECT ST_MultiplyZ('MULTIPOINT((190 300 100), (10 11 50))',
                    0.1);
-- Answer: MULTIPOINT((190 300 10), (10 11 5))
{% endhighlight %}

##### See also

* [`ST_UpdateZ`](../ST_UpdateZ),
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_MultiplyZ.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
