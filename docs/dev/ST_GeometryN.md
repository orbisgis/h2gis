---
layout: docs
title: ST_GeometryN
category: h2spatial/properties
description: Return a element of a <code>GEOMETRYCOLLECTION</code>
prev_section: ST_ExteriorRing
next_section: ST_GeometryType
permalink: /docs/dev/ST_GeometryN/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_GeometryN(GEOMETRY geom,integer i);
{% endhighlight %}

### Description

Returns a element number `i` of a `GEOMETRYCOLLECTION`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_GeometryN('MULTIPOLYGON(((0 0, 3 -1, 1.5 2, 0 0)), 
                     ((1 2, 4 2, 4 6, 1 6, 1 2)))', 1);
-- Answer: POLYGON((0 0, 3 -1, 1.5 2, 0 0))

SELECT ST_GeometryN('MULTILINESTRING((1 1, 1 6, 2 2, -1 2),
                     (1 2, 4 2, 4 6))', 2);
-- Answer: LINESTRING(1 2, 4 2, 4 6)

SELECT ST_GeometryN('MULTIPOINT((0 0), (1 6), (2 2), (1 2))', 2);
-- Answer: POINT(1 6)

SELECT ST_GeometryN('GEOMETRYCOLLECTION(
                     MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
                     LINESTRING(2 6, 6 2), POINT(4 4), 
                     POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))',3);
-- Answer: POINT(4 4)
{% endhighlight %}

##### See also

* [`ST_NumGeometries`](../ST_NumGeometries)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_GeometryN.java" target="_blank">Source code</a>
