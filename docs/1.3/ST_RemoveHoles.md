---
layout: docs
title: ST_RemoveHoles
category: geom2D/edit-geometries
is_function: true
description: Remove a Geometry's holes
prev_section: ST_Normalize
next_section: ST_RemovePoints
permalink: /docs/1.3/ST_RemoveHoles/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_RemoveHoles(GEOMETRY geom);
{% endhighlight %}

### Description

Removes all holes in `geom`.
Returns `geom` untouched if it contains no holes.
Returns `NULL` if `geom` is not a `POLYGON` or `MULTIPOLYGON`.

### Examples

{% highlight mysql %}
SELECT ST_RemoveHoles(
            'POLYGON((1 5, 0 4, 0 1, 1 0, 4 0, 4 2, 5 4, 5 4, 1 5),
                     (1 3, 1 4, 2 4, 2 3, 1 3),
                     (2 2, 1 1, 2 1, 2 2))');
-- Answer:   POLYGON((1 5, 0 4, 0 1, 1 0, 4 0, 4 2, 5 4, 5 4, 1 5))
{% endhighlight %}

<img class="displayed" src="../ST_RemoveHoles.png"/>

##### Non-examples

{% highlight mysql %}
-- Here are no holes to remove:
SELECT ST_RemoveHoles(
          'POLYGON((1 5, 0 4, 0 1, 1 0, 4 0, 4 2, 5 4, 5 4, 1 5))');
-- Answer: POLYGON((1 5, 0 4, 0 1, 1 0, 4 0, 4 2, 5 4, 5 4, 1 5))

-- Returns NULL for POINTS:
SELECT ST_RemoveHoles('POINT(1 5)');
-- Answer: NULL
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_RemoveHoles.java" target="_blank">Source code</a>
