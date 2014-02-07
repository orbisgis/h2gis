---
layout: docs
title: ST_NumGeometries
category: h2spatial/properties
description: Returns the number of Geometries inside a <code>GEOMETRYCOLLECTION</code>
prev_section: ST_M
next_section: ST_NumInteriorRing
permalink: /docs/dev/ST_NumGeometries/
---

### Signatures

{% highlight mysql %}
int ST_NumGeometries(GEOMETRY geom);
int ST_NumGeometries(GEOMETRYCOLLECTION geom);
{% endhighlight %}

### Description

Returns the number of Geometries inside a `GEOMETRYCOLLECTION` and MultiGeometry.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_NumGeometries('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 1

SELECT ST_NumGeometries('MULTILINESTRING(
                             (0 2, 3 2, 3 6, 0 6, 0 1), 
                             (5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: 2

SELECT ST_NumGeometries('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0), 
                                 (1 1, 2 1, 2 5, 1 5, 1 1), 
                                 (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: 1

SELECT ST_NumGeometries('MULTIPOLYGON(((0 0, 10 0, 10 6, 0 6, 0 0)), 
                                      ((1 1, 2 1, 2 5, 1 5, 1 1)), 
                                      ((8 5, 8 4, 9 4, 9 5, 8 5)))');
-- Answer: 3

SELECT ST_NumGeometries('GEOMETRYCOLLECTION(
                           MULTIPOINT((4 4), (1 1), (1 0), (0 3)),  
                           LINESTRING(2 6, 6 2), 
                           POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 3

SELECT ST_NumGeometries('MULTIPOINT((0 2), (3 2), (3 6), (0 6), 
                                    (0 1), (5 0), (7 0))');
-- Answer: 7
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_NumGeometries.java" target="_blank">Source code</a>
