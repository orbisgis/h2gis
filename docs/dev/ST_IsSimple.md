---
layout: docs
title: ST_IsSimple
category: h2spatial/properties
description: Return true if a <code>GEOMETRY</code> is simple
prev_section: ST_IsRing
next_section: ST_Length
permalink: /docs/dev/ST_IsSimple/
---

### Signatures

{% highlight mysql %}
boolean ST_IsSimple(GEOMETRY geom);
{% endhighlight %}

### Description

Returns true if a `GEOMETRY` is simple.
no anomalous geometric points, such as self intersection or self tangency

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_IsSimple('POINT (4 4)');
SELECT ST_IsSimple('MULTIPOINT empty)');
SELECT ST_IsSimple('LINESTRING(2 1, 1 3, 6 6, 12 3, 5 2, 2 1)');
SELECT ST_IsSimple('MULTILINESTRING((0 2, 3 2, 3 6, 0 6, 0 2), 
                                     (5 0, 7 0, 7 1, 5 1, 5 0))');
SELECT ST_IsSimple('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)), 
                                 ((5 0, 7 0, 7 1, 5 1, 5 0)))');
SELECT ST_IsSimple('GEOMETRYCOLLECTION(
                      MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
                      LINESTRING(2 6, 6 2), 
                      POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
SELECT ST_IsSimple('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0), 
                            (1 1, 2 1, 2 5, 1 5, 1 1), 
                            (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: TRUE

SELECT ST_IsSimple('LINESTRING (2 1, 1 3, 6 6, 5 10, 5 2, 2 1)');
-- Answer: FALSE
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_IsSimple.java" target="_blank">Source code</a>
