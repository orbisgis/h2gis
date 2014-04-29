---
layout: docs
title: ST_RemoveRepeatedPoints
category: h2spatial-ext/edit-geometries
description: Return a version of the given Geometry with duplicated points removed
prev_section: ST_RemovePoint
next_section: ST_Reverse
permalink: /docs/dev/ST_RemoveRepeatedPoints/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_RemoveRepeatedPoints(GEOMETRY geom);
{% endhighlight %}

### Description
Removes duplicated points on a `GEOMETRY`.

### Examples

{% highlight mysql %}
SELECT ST_RemoveRepeatedPoints('MULTIPOINT((4 4), (1 1), (1 0), 
                                           (0 3), (4 4))');
-- Answer:  MULTIPOINT((4 4), (1 1), (1 0),(0 3), (4 4))
-- The POINT(4 4) is not duplicated is two geometries 
--  independent then it's not removed.

SELECT ST_RemoveRepeatedPoints('LINESTRING(1 1, 2 2, 2 2, 1 3, 
                                           1 3, 3 3, 3 3, 5 2, 
                                           5 2, 5 1)');
-- Answer: LINESTRING(1 1, 2 2, 1 3, 3 3, 5 2, 5 1) 

SELECT ST_RemoveRepeatedPoints('POLYGON((2 4, 1 3, 2 1, 2 1, 
                                         6 1, 6 3, 4 4, 4 4, 
                                         2 4))');
-- Answer: POLYGON((2 4, 1 3, 2 1, 6 1, 6 3, 4 4, 2 4))

SELECT ST_RemoveRepeatedPoints('GEOMETRYCOLLECTION(
                      POLYGON((1 2, 4 2, 4 6, 1 6, 1 6, 1 2)), 
                      LINESTRING(2 6, 6 2), 
                      MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: GEOMETRYCOLLECTION(POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)), 
--                            LINESTRING(2 6, 6 2), 
--                            MULTIPOINT((4 4), (1 1), (1 0), 
--                                        (0 3)))
{% endhighlight %}

##### See also

* [`ST_RemovePoint`](../ST_RemovePoint)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_RemoveRepeatedPoints.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
