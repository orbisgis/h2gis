---
layout: docs
title: ST_3DLength
category: geom3D/distance-functions
is_function: true
description: Return the 3D length or the 3D perimeter of a Geometry
prev_section: geom3D/distance-functions
next_section: ST_SunPosition
permalink: /docs/dev/ST_3DLength/
---

### Signature

{% highlight mysql %}
DOUBLE ST_3DLength(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the 3D length (of a `LINESTRING`) or the 3D perimeter (of a `POLYGON`).
In the case of a 2D Geometry, `ST_3DLength` returns the same value as
`ST_Length`.

### Examples

{% highlight mysql %}
SELECT ST_3DLength('LINESTRING(1 4, 15 7, 16 17)');
-- Answer:    24.367696684397245 = SQRT(205) + SQRT(101)

SELECT ST_3DLength('LINESTRING(1 4 3, 15 7 9, 16 17 22)');
-- Answer:    31.955851421415005 = SQRT(241) + SQRT(270)

SELECT ST_3DLength('MULTILINESTRING((1 4 3, 15 7 9, 16 17 22),
                                    (0 0 0, 1 0 0, 1 2 0, 0 2 1))');
-- Answer:    36.3700649837881 = SQRT(241) + SQRT(270) + 3 + SQRT(2)

SELECT ST_3DLength('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))');
-- Answer:    6.0

SELECT ST_3DLength('POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))');
-- Answer:    9.048627177541054 = SQRT(2) + 2 * SQRT(5) + SQRT(10)

SELECT ST_3DLength('MULTIPOLYGON(((0 0 0, 3 2 0, 3 2 2, 0 0 2, 0 0 0),
                                  (-1 1 0, -1 3 0, -1 3 4, -1 1 4, -1 1 0)))');
-- Answer:    23.21110255092798 = 16 + 2 * SQRT(13)

SELECT ST_3DLength('GEOMETRYCOLLECTION(
                      LINESTRING(1 4 3, 15 7 9, 16 17 22),
                      POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))');
-- Answer:    41.004478598956055 = SQRT(241) + SQRT(270) + SQRT(2) + 2 * SQRT(5) + SQRT(10)
{% endhighlight %}

##### See also

* [`ST_Length`](../ST_Length)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_3DLength.java" target="_blank">Source code</a>
