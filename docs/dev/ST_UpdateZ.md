---
layout: docs
title: ST_UpdateZ
category: h2spatial-ext/edit-geometries
description: Return a Geometry with the z values updated
prev_section: ST_Reverse3DLine
next_section: ST_ZUpdateLineExtremities
permalink: /docs/dev/ST_UpdateZ/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_UpdateZ(GEOMETRY geom, double z);
GEOMETRY ST_UpdateZ(GEOMETRY geom, double z, int updateCondition);
{% endhighlight %}

### Description
Replaces the z value of (each vertex of) the geometric parameter to the corresponding value given by a field.The first argument is used to give the new z values.The second argument is a condition: 
* 1 to replace all z values.
* 2 to replace all z values excepted the NaN values.
* 3 to replace only the NaN z values.

If the `updateCondition` is not defined, `z` replace all z value. 

### Examples

{% highlight mysql %}
SELECT ST_UpdateZ('POINT(190 300 1)'::GEOMETRY, 10, 4);
-- Answer: Available values are 1, 2 or 3.

SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10);
-- Answer: MULTIPOINT((190 300 10), (10 11 10))

SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10, 1);
-- Answer: MULTIPOINT((190 300 10), (10 11 10))

SELECT ST_UpdateZ('POLYGON((1 1, 1 7 8, 7 7 -1, 7 1 -1, 1 1))',
                   10, 2);
-- Answer: POLYGON((1 1, 1 7 10, 7 7 10, 7 1 10, 1 1))

SELECT ST_UpdateZ('LINESTRING(250 250 10, 280 290, 300 230 0, 
                              340 300)', 5, 3);
-- Answer: LINESTRING(250 250 10, 280 290 5, 300 230 0, 
--                     340 300 5)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_UpdateZ.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
