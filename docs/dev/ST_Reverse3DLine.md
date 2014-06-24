---
layout: docs
title: ST_Reverse3DLine
category: h2spatial-ext/edit-geometries
description: Return a Geometry with vertex order reversed according the Z values
prev_section: ST_Reverse
next_section: ST_UpdateZ
permalink: /docs/dev/ST_Reverse3DLine/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Reverse3DLine(GEOMETRY geom);
GEOMETRY ST_Reverse3DLine(GEOMETRY geom, VARCHAR orderReverse);
{% endhighlight %}

### Description
Reverses order ascending or descending of z values of a `LINESTRING` or `MULTILINESTRING` according the start and the end z values.
If the `orderReverse` is not defined, it's ascending order which applies.
If the  start or the end z values are equal to NaN return the input Geometry.

### Examples

{% highlight mysql %}
SELECT ST_Reverse3DLine('POLYGON((190 300, 140 180, 300 110,
                                   313 117, 430 270, 380 430,
                                   190 300))');
-- Answer: NULL

SELECT ST_Reverse3DLine('LINESTRING(1 1, 1 6 2, 2 2, -1 2)');
-- Answer: LINESTRING(1 1, 1 6 2, 2 2, -1 2)

SELECT ST_Reverse3DLine('LINESTRING(105 353 10, 150 180,
                                    300 280 0)');
-- Answer: LINESTRING(300 280 0, 150 180, 105 353 10)

SELECT ST_Reverse3DLine('LINESTRING(105 353 10, 150 180,
                                    300 280 0)', 'desc');
-- Answer: LINESTRING(105 353 10, 150 180, 300 280 0)

SELECT ST_Reverse3DLine('LINESTRING(105 353 0, 150 180,
                                    300 280 10)', 'desc');
-- Answer: LINESTRING(300 280 10, 150 180, 105 353 0)

SELECT ST_Reverse3DLine('MULTILINESTRING((1 1 1, 1 6 2, 2 2 1,
                                          -1 2 3),
                                         (1 2 0, 4 2, 4 6 2))',
                        'desc');
-- Answer: MULTILINESTRING((-1 2 3, 2 2 1, 1 6 2, 1 1 1),
--                         (4 6 2, 4 2, 1 2 0))
{% endhighlight %}

##### See also

* [`ST_Reverse`](../ST_Reverse)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_Reverse3DLine.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
