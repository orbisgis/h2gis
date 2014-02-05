---
layout: docs
title: ST_Z
category: h2spatial/properties
description: Return the first Z coordinate
prev_section: ST_Y
next_section: h2spatial-ext/affine-transformations
permalink: /docs/dev/ST_Z/
---

### Signature

{% highlight mysql %}
double ST_Z(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the first Z coordinate relative of the order of writing in the Geometry.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Z('LINESTRING(2 1 0, 1 3 3, 5 2 1)');
-- Answer: 0.0

SELECT ST_Z('POLYGON((5 0 2, 7 0 4, 7 1 3, 5 1 6, 5 0 1))');
-- Answer: 2.0

CREATE TABLE temp As SELECT ST_ExteriorRing(
    'POLYGON((5 0 2, 7 0 4, 7 1 3, 5 1 6, 5 0 1))') As resu;
CREATE TABLE temp1 As SELECT ST_PointN(resu,3) As resu1 from temp;
SELECT ST_Z(resu1) from temp1;
DROP TABLE temp, temp1;
-- Answer: 3.0

SELECT ST_Z('GEOMETRYCOLLECTION(
                 LINESTRING(2 1 0, 1 3 3, 5 2 1), 
                 MULTIPOINT((4 4 3), (1 1 1), (1 0 2), (0 3 6)), 
                 POLYGON((1 2 2, 4 2 5, 4 6 3, 1 6 1, 1 2 1)))');
-- Answer: 0.0
{% endhighlight %}

##### See also

* [`ST_X`](../ST_X), [`ST_Y`](../ST_Y), [`ST_ExteriorRing`](../ST_ExteriorRing),[`ST_InteriorRingN`](../ST_InteriorRingN), [`ST_GeometryN`](../ST_GeometryN), [`ST_PointN`](../ST_PointN), 
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Z.java" target="_blank">Source code</a>
