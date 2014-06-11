---
layout: docs
title: ST_ZUpdateLineExtremities
category: h2spatial-ext/edit-geometries
description: Return a Geometry with the start and end z values updated
prev_section: ST_UpdateZ
next_section: h2spatial-ext/process-geometries
permalink: /docs/dev/ST_ZUpdateLineExtremities/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_ZUpdateLineExtremities(GEOMETRY geom, double startZ, 
                                   double endZ);
GEOMETRY ST_ZUpdateLineExtremities(GEOMETRY geom, double startZ, 
                                   double endZ, boolean interpolate);
{% endhighlight %}

### Description
Replaces the start and end z values of a `LINESTRING` or `MULTILINESTRING`. By default the other z values are interpolated according the length of the line. 
If the `interpolate` is true the vertices are interpolated according the `startZ` and `endZ` values.
Set false if you want to update only the start and end z values.

### Examples

{% highlight mysql %}
SELECT ST_ZUpdateLineExtremities('POLYGON((1 1, 1 7, 7 7 -1, 
                                           7 1 -1, 1 1))', 
                                  10, 15);
-- Answer: null

SELECT ST_ZUpdateLineExtremities('LINESTRING(250 250, 280 290)',
                                  40, 10);
-- Answer: LINESTRING(250 250 40, 280 290 10)

SELECT ST_ZUpdateLineExtremities('MULTILINESTRING((1 1 1, 1 6 2, 
                                                   2 2 1, -1 2 3),
                                                  (1 2 0, 4 2, 
                                                   4 6 2))', 
                                 0, 10);
-- Answer: MULTILINESTRING((1 1 0, 1 6 3.6889, 2 2 2.4746, 
--                          -1 2 10), 
--                         (1 2 0, 4 2 5.7142, 4 6 10))

SELECT ST_ZUpdateLineExtremities('LINESTRING(0 0, 5 0 1, 15 0)', 
                                  0, 20);
-- Answer: LINESTRING(0 0 0, 5 0 13.333333333333332, 15 0 20)

SELECT ST_ZUpdateLineExtremities('LINESTRING(0 0, 5 0 1, 15 0)',
                                  0, 20, 'true');
-- Answer: LINESTRING(0 0 0, 5 0 13.333333333333332, 15 0 20)

SELECT ST_ZUpdateLineExtremities('LINESTRING(0 0, 5 0 1, 10 0)',
                                  0, 20, 'false');
-- Answer: LINESTRING(0 0 0, 5 0 1, 10 0 20)
{% endhighlight %}

##### See also
* [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_ZUpdateLineExtremities.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
