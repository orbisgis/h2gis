---
layout: docs
title: ST_Split
category: h2spatial-ext/process-geometries
description: Return a Geometry resulting by splitting a Geometry
prev_section: ST_Snap
next_section: h2spatial-ext/measure-distance
permalink: /docs/dev/ST_Split/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Split(GEOMETRY geomA, GEOMETRY geomB);
(MULTI)LINESTRING ST_Split((MULTI)LINESTRING geomA, POINT geomB, 
double tolerance);
{% endhighlight %}

### Description
Returns a `GEOMETRY` resulting by splitting a `GEOMETRY`.
Split a GeometryA according a GeometryB.
Supported operations are : 

* a LINESTRING by a POINT 
It's possible to using a snapping `tolerance`. If a `tolerance` is not define, the `tolerance` 10E-6 is used to snap the cutter point.
* a LINESTRING by a LINESTRING  
* a POLYGON by a LINESTRING.

### Examples

#####LINESTRING by a POINT 
{% highlight mysql %}
SELECT ST_Split('LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8, 
                            25 8, 30 8, 50 8, 100 8)', 
                'POINT(1.5 4 )');
-- Answer: MULTILINESTRING EMPTY

SELECT ST_Split('LINESTRING(3 3, 4 5, 2 2)', 
                'POINT(2 4)', 
                4);
-- Answer: MULTILINESTRING ((3 3, 4 5, 2.9230769230769234 3.3846153846153846), (2.9230769230769234 3.3846153846153846, 2 2))                

SELECT ST_Split('LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 
                            20 8, 25 8, 30 8, 50 8, 100 8)', 
                'POINT(1.5 4 )', 
                4);
-- Answer: MULTILINESTRING((0 8, 1 8, 1.5 8), 
--                         (1.5 8, 3 8, 8 8, 10 8, 20 8, 
--                          25 8, 30 8, 50 8, 100 8))
{% endhighlight %}

#####LINESTRING by a  LINESTRING
{% highlight mysql %}
SELECT ST_Split('LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8, 
                            25 8, 30 8, 50 8, 100 8)', 
                'LINESTRING(50 -50, 50 50)');
-- Answer: MULTILINESTRING ((0 8, 1 8, 3 8, 8 8, 10 8, 20 8, 
--                           25 8, 30 8, 50 8), 
--                          (50 8, 100 8))
 
SELECT ST_Split('LINESTRING(0 0, 100 0)', 
                'LINESTRING(50 -50, 50 50)');
-- Answer: MULTILINESTRING ((0 0, 50 0), (50 0, 100 0))

SELECT ST_Split('LINESTRING(50 0, 100 0)', 
                'LINESTRING(50 50, 100 50)');
-- Answer: LINESTRING (50 0, 100 0)
{% endhighlight %}

#####POLYGON by a  LINESTRING
{% highlight mysql %}
SELECT ST_Split('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))', 
                'LINESTRING(5 0, 5 10)');
-- Answer: MULTIPOLYGON (((5 0, 5 10, 10 10, 10 0, 5 0)), 
                         ((5 0, 0 0, 0 10, 5 10, 5 0)))

SELECT ST_Split('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))', 
                'LINESTRING (5 1, 5 12)');
-- Answer: null

SELECT ST_Split('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0), 
                          (2 2, 7 2, 7 7, 2 7, 2 2))', 
                'LINESTRING(5 0, 5 10)');
-- Answer: MULTIPOLYGON (((5 0, 0 0, 0 10, 5 10, 5 7, 2 7, 
                           2 2, 5 2, 5 0)), 
                         ((5 10, 10 10, 10 0, 5 0, 5 2, 7 2, 
                           7 7, 5 7, 5 10)))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/processing/ST_Split.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
