---
layout: docs
title: ST_ConvexHull
category: geom2D/operators
is_function: true
description: Compute the smallest convex <code>POLYGON</code> that contains all the points in the Geometry
prev_section: ST_Buffer
next_section: ST_Difference
permalink: /docs/1.3.2/ST_ConvexHull/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_ConvexHull(GEOMETRY geom)
{% endhighlight %}

### Description

Computes the smallest convex `POLYGON` that contains all the points of `geom`.
`geom` can be a set of `POINT`s, `LINESTRING`s, `POLYGON`s or a
`GEOMETRYCOLLECTION`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_ConvexHull('GEOMETRYCOLLECTION(
                        POINT(1 2),
                        LINESTRING(1 4, 4 7),
                        POLYGON((3 1, 7 1, 7 6, 3 1)))');

-- Answer: POLYGON((3 1, 7 1, 7 6, 4 7, 1 4, 1 2, 3 1))
{% endhighlight %}

<img class="displayed" src="../ST_ConvexHull.png"/>

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/operators/ST_ConvexHull.java" target="_blank">Source code</a>
