---
layout: docs
title: ST_RemoveHoles
category: h2spatial-ext/edit-geometries
description: Return a Geometry without holes
prev_section: ST_Normalize
next_section: ST_RemovePoint
permalink: /docs/dev/ST_RemoveHoles/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_RemoveHoles(GEOMETRY geom);
{% endhighlight %}

### Description
Remove all holes in a `POLYGON` or a `MULTIPOLYGON`.
If the Geometry doesn't contain any hole return the input Geometry.
If the input Geometry is not a POLYGON or MULTIPOLYGON return null.

### Examples

{% highlight mysql %}
SELECT ST_RemoveHoles('POINT(1 5)');
-- Answer: null

SELECT ST_RemoveHoles('POLYGON((1 5, 0 4, 0 1, 1 0, 4 0,
                                4 2, 5 4, 5 4, 1 5))');
-- Answer: POLYGON((1 5, 0 4, 0 1, 1 0, 4 0, 4 2, 5 4,
--                  5 4, 1 5))

SELECT ST_RemoveHoles('POLYGON((1 5, 0 4, 0 1, 1 0, 4 0,
                                4 2, 5 4, 5 4, 1 5),
                               (1 3, 1 4, 2 4, 2 3, 1 3),
                               (2 2, 1 1, 2 1, 2 2))');
-- Answer: POLYGON((1 5, 0 4, 0 1, 1 0, 4 0, 4 2, 5 4, 5 4, 1 5))
{% endhighlight %}

<img class="displayed" src="../ST_RemoveHoles.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_RemoveHoles.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
