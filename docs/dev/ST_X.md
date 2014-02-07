---
layout: docs
title: ST_X
category: h2spatial/properties
description: Return the first X coordinate
prev_section: ST_StartPoint
next_section: ST_Y
permalink: /docs/dev/ST_X/
---

### Signature

{% highlight mysql %}
double ST_X(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the first X coordinate relative of the order of writing in the Geometry.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_X('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: 4.0

CREATE TABLE temp As SELECT ST_geometryN(
    'MULTIPOINT((4 4), (1 1), (1 0), (0 3)))',2) As resu;
SELECT ST_X(resu) from temp;
DROP TABLE temp;
-- Answer: 1.0

SELECT ST_X('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 2.0

CREATE TABLE temp As SELECT ST_PointN(
    'LINESTRING(2 1, 1 3, 5 2)',3) As resu;
SELECT ST_X(resu) from temp;
DROP TABLE temp;
-- Answer: 5.0

SELECT ST_X('POLYGON((5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: 5.0

CREATE TABLE temp As SELECT ST_ExteriorRing(
    'POLYGON((5 0, 7 0, 7 1, 5 1, 5 0))') As resu;
CREATE TABLE temp1 As SELECT ST_PointN(resu,3) As resu1 from temp;
SELECT ST_X(resu1) from temp1;
DROP TABLE temp, temp1;
-- Answer: 7.0

SELECT ST_X('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)), 
                          ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: 0.0

SELECT ST_X('GEOMETRYCOLLECTION(
               MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
               LINESTRING(2 1, 1 3, 5 2), 
               POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 4.0

{% endhighlight %}

##### See also

* [`ST_Y`](../ST_Y), [`ST_Z`](../ST_Z), [`ST_ExteriorRing`](../ST_ExteriorRing),[`ST_InteriorRingN`](../ST_InteriorRingN), [`ST_GeometryN`](../ST_GeometryN), [`ST_PointN`](../ST_PointN),
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_X.java" target="_blank">Source code</a>
