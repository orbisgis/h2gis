---
layout: docs
title: ST_Difference
category: h2spatial/operators
description: Compute the difference between two geometries
prev_section: ST_ConvexHull
next_section: ST_Intersection
permalink: /docs/dev/ST_Difference/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Difference(geomA, geomB)
{% endhighlight %}

### Description

Computes the difference between `geomA` and `geomB`.

{% include sfs-1-2-1.html %}

### Examples

| geomA Polygon | geomB Polygon |
| ----|---- |
| POLYGON((1 1, 7 1, 7 6, 1 6, 1 1)) | POLYGON((3 2, 8 2, 8 8, 3 8, 3 2)) |

{% highlight mysql %}
SELECT ST_Difference(geomA, geomB);
-- Answer:    POLYGON((1 1, 7 1, 7 2, 3 2, 3 6, 1 6, 1 1))

SELECT ST_Difference(geomB, geomA);
-- Answer:    POLYGON((7 2, 8 2, 8 8, 3 8, 3 6, 7 6, 7 2))
{% endhighlight %}

<img class="displayed" src="../ST_Difference.png"/>

##### See also

* [`ST_SymDifference`](../ST_SymDifference)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/operators/ST_Difference.java" target="_blank">Source code</a>
