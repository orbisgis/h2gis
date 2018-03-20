---
layout: docs
title: ST_3DPerimeter
category: geom3D/distance-functions
is_function: true
description: Return the 3D perimeter of a (multi)polygon
prev_section: ST_3DLength
next_section: ST_SVF
permalink: /docs/1.2/ST_3DPerimeter/
---

### Signature

{% highlight mysql %}
DOUBLE ST_Perimeter(GEOMETRY poly);
{% endhighlight %}

### Description

Returns the 3D perimeter of a `polygon` or a `multipolygon`.

 that .

Perimeter is measured in the units of the spatial reference system.

Remarks
*  In the case of a 2D geometry, `ST_3DPerimeter` returns the same value as `ST_Perimeter`,
*  `Polygons` and `multipolygons` within `geometrycollection` are accepted.

### Examples

{% highlight mysql %}
SELECT ST_3DPerimeter('POLYGON((1 2 0, 4 2 1, 4 6 3, 1 6 5, 1 2 0))');
-- Answer: 17.63

SELECT ST_3DPerimeter('POLYGON((1 2, 4 2, 4 6, 1 6, 1 2))');
-- Answer: 14.0

SELECT ST_3DPerimeter('MULTIPOLYGON(((0 2 0, 3 2 1, 3 6 2, 0 6 3, 0 2 0)),
                                    ((5 0 1, 7 0 3, 7 1 4, 5 1 2, 5 0 1)))');
-- Answer: 23.93

SELECT ST_3DPerimeter('GEOMETRYCOLLECTION(
                    MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                    LINESTRING(2 1, 1 3, 5 2),
                    POLYGON((1 2 0, 4 2 1, 4 6 2, 1 6 3, 1 2 0)))');
-- Answer: 15.44

SELECT ST_3DPerimeter('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 0
{% endhighlight %}

##### See also

* [`ST_Perimeter`](../ST_Perimeter), [`ST_3DLength`](../ST_3DLength), [`ST_Length`](../ST_Length)
* <a href="https://github.com/orbisgis/h2gis/blob/v1.2.4/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_Perimeter.java" target="_blank">Source code</a>
