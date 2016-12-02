---
layout: docs
title: ST_RemoveRepeatedPoints
category: geom2D/edit-geometries
is_function: true
description: Remove repeated points from a Geometry
prev_section: ST_RemovePoint
next_section: ST_Reverse
permalink: /docs/dev/ST_RemoveRepeatedPoints/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_RemoveRepeatedPoints(GEOMETRY geom);
{% endhighlight %}

### Description

Removes repeated points from `geom`.

### Examples

{% highlight mysql %}
SELECT ST_RemoveRepeatedPoints(
            'LINESTRING(1 1, 2 2, 2 2, 1 3, 1 3,
                        3 3, 3 3, 5 2, 5 2, 5 1)');
-- Answer:   LINESTRING(1 1, 2 2,      1 3,
--                      3 3,      5 2,      5 1)

SELECT ST_RemoveRepeatedPoints(
            'POLYGON((2 4, 1 3, 2 1, 2 1, 6 1,
                      6 3, 4 4, 4 4, 2 4))');
-- Answer:   POLYGON((2 4, 1 3, 2 1,      6 1,
--                    6 3, 4 4,      2 4))

SELECT ST_RemoveRepeatedPoints(
        'GEOMETRYCOLLECTION(
             POLYGON((1 2, 4 2, 4 6, 1 6, 1 6, 1 2)),
             MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: GEOMETRYCOLLECTION(
--           POLYGON((1 2, 4 2, 4 6, 1 6,      1 2)),
--           MULTIPOINT((4 4), (1 1), (1 0), (0 3)))

-- Here POINT(4 4) is not removed since it is an independant
-- geometry:
SELECT ST_RemoveRepeatedPoints(
            'MULTIPOINT((4 4), (1 1), (1 0), (0 3), (4 4))');
-- Answer:   MULTIPOINT((4 4), (1 1), (1 0), (0 3), (4 4))
{% endhighlight %}

##### See also

* [`ST_RemovePoints`](../ST_RemovePoints)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_RemoveRepeatedPoints.java" target="_blank">Source code</a>
