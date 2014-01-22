---
layout: docs
title: ST_SymDifference
category: h2spatial/operators
description: Compute the symmetric difference between two Geometries
prev_section: ST_Intersection
next_section: ST_Union
permalink: /docs/dev/ST_SymDifference/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_SymDifference(geomA, geomB)
{% endhighlight %}

### Description

Computes the symmetric difference between `geomA` and `geomB`.

{% include sfs-1-2-1.html %}

### Examples

| geomA Polygon | geomB Polygon |
| ----|---- |
| POLYGON((1 1, 7 1, 7 6, 1 6, 1 1)) | POLYGON((3 2, 8 2, 8 8, 3 8, 3 2)) |

{% highlight mysql %}
SELECT ST_Difference(geomA, geomB);
-- Answer: MULTIPOLYGON(((1 1, 7 1, 7 2, 3 2, 3 6, 1 6, 1 1)), 
--                     ((7 2, 8 2, 8 8, 3 8, 3 6, 7 6, 7 2)))
{% endhighlight %}

<img class="displayed" src="../ST_SymDifference.png"/>

##### See also

* [`ST_Difference`](../ST_Difference)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/operators/ST_SymDifference.java" target="_blank">Source code</a>
