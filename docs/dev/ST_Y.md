---
layout: docs
title: ST_Y
category: geom2D/properties
is_function: true
description: Return the y-value of the first coordinate of a Geometry
prev_section: ST_XMin
next_section: ST_YMax
permalink: /docs/dev/ST_Y/
---

### Signature

{% highlight mysql %}
DOUBLE ST_Y(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the y-value of the first coordinate of `geom`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Y('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: 4.0

SELECT ST_Y(
    ST_GeometryN('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))', 2));
-- Answer: 1.0

SELECT ST_Y('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 1.0

SELECT ST_Y(ST_PointN('LINESTRING(2 1, 1 3, 5 2)', 3));
-- Answer: 2.0

SELECT ST_Y('POLYGON((5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: 0.0

SELECT ST_Y(
    ST_PointN(
        ST_ExteriorRing('POLYGON((5 0, 7 0, 7 1, 5 1, 5 0))'), 3));
-- Answer: 1.0

SELECT ST_Y('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)),
                          ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: 2.0

SELECT ST_Y('GEOMETRYCOLLECTION(
               MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
               LINESTRING(2 1, 1 3, 5 2),
               POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 4.0
{% endhighlight %}

##### See also

* [`ST_X`](../ST_X), [`ST_Z`](../ST_Z)
* [`ST_GeometryN`](../ST_GeometryN), [`ST_PointN`](../ST_PointN), [`ST_ExteriorRing`](../ST_ExteriorRing)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Y.java" target="_blank">Source code</a>
