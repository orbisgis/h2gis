---
layout: docs
title: ST_NumInteriorRings
category: h2spatial/properties
description: Return the holes' number in a Geometry
prev_section: ST_NumInteriorRing
next_section: ST_NumPoints
permalink: /docs/dev/ST_NumInteriorRings/
---

### Signatures

{% highlight mysql %}
int ST_NumInteriorRings(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the holes' number in a `GEOMETRY`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_NumInteriorRings('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                                    (1 1, 2 1, 2 5, 1 5, 1 1), 
                                    (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: 2

SELECT ST_NumInteriorRings('MULTIPOLYGON(
                                ((0 0, 10 0, 10 6, 0 6, 0 0), 
                                ((1 1, 2 1, 2 5, 1 5, 1 1)), 
                                ((8 5, 8 4, 9 4, 9 5, 8 5)))');
-- Answer: 0

SELECT ST_NumInteriorRings('MULTIPOLYGON(
                                ((0 0, 10 0, 10 6, 0 6, 0 0),
                                 (1 1, 2 1, 2 5, 1 5, 1 1)), 
                                ((1 1, 2 1, 2 5, 1 5, 1 1)), 
                                ((8 5, 8 4, 9 4, 9 5, 8 5)))');
-- Answer: 1

SELECT ST_NumInteriorRings(
     'GEOMETRYCOLLECTION(MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
                         LINESTRING(2 6, 6 2), 
                         POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 0

SELECT ST_NumInteriorRings(
     'GEOMETRYCOLLECTION(MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
                         LINESTRING(2 6, 6 2), 
                         POLYGON((1 2, 4 2, 4 6, 1 6, 1 2),
                                 (2 4, 3 4, 3 5, 2 5, 2 4)))');
-- Answer: 1
{% endhighlight %}

##### See also

* [`ST_NumInteriorRing`](../ST_NumInteriorRing)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_NumInteriorRings.java" target="_blank">Source code</a>
