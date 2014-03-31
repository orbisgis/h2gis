---
layout: docs
title: ST_SimplifyPreserveTopology
category: h2spatial-ext/process-geometries
description: Return a simplified version of the given Geometry where the topology is preserved
prev_section: ST_Simplify
next_section: ST_Snap
permalink: /docs/dev/ST_SimplifyPreserveTopology/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_SimplyPreserveTopology(GEOMETRY geom, 
double distance);
{% endhighlight %}

### Description
Return a simplified Geometry and ensures that the result is a valid Geometry having the same dimension and number of components as the input, and with the components having the same topological relationship.

<div class="note">
    <h5>
    If you don't want preserve topology you can use ST_Simplify.</h5>
</div>

### Examples

{% highlight mysql %}
SELECT ST_SimplifyPreserveTopology('MULTIPOINT((190 300), 
                                               (10 11))', 
                                    4);
-- Answer: MULTIPOINT((190 300), (10 11))

SELECT ST_SimplifyPreserveTopology('LINESTRING(250 250, 280 290, 
                                               300 230, 340 300, 
                                               360 260, 440 310, 
                                               470 360, 604 286)', 
                                    40);
-- Answer: LINESTRING(250 250, 280 290, 300 230, 470 360, 604 286)

SELECT ST_SimplifyPreserveTopology('POLYGON((1 2, 4 2, 1 4, 
                                             3 5, 6 5, 4 4, 
                                             5 3, 4 3, 1 2))', 
                                    1);
-- Answer: POLYGON((3.1818181818181817 2.5454545454545454,  
--                  1 4, 6 5, 4 4, 5 3, 
--                  3.1818181818181817 2.5454545454545454))
{% endhighlight %}

 | 	          Geom Polygon                     | 
 |----------------------------------------------|
 | POLYGON ((24 39, 8 25, 43 19, 47 44, 35 36,  |
 |           56 30, 33 3, 15 11, 35 26, 24 39)) |

{% highlight mysql %}
SELECT st_simplifypreservetopology(geom, 10);
SELECT st_simplifypreservetopology(geom, 20);
SELECT st_simplifypreservetopology(geom, 30);
-- Answer: POLYGON ((24 39, 8 25, 43 19, 47 44, 35 36, 
--                   56 30, 33 3, 15 11, 35 26, 24 39))
-- POLYGON ((24 39, 8 25, 43 19, 47 44, 56 30, 33 3, 
--           15 11, 35 26, 24 39))
-- POLYGON ((24 39, 56 30, 33 3, 15 11, 24 39))

{% endhighlight %}

<img class="displayed" src="../ST_SimplifyPreserveTopolgy.png"/>

##### Comparison with [`ST_Simplify`](../ST_Simplify)

{% include simplify-simplifypreserve-cf.html %}

##### See also

* [`ST_Simplify`](../ST_Simplify)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/processing/ST_SimplifyPreserveTopology.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>

