---
layout: docs
title: ST_Dimension
category: geom2D/properties
is_function: true
description: Return the dimension of a Geometry
prev_section: ST_CoordDim
next_section: ST_Distance
permalink: /docs/dev/ST_Dimension/
---

### Signature

{% highlight mysql %}
INT ST_Dimension(GEOMETRY geom);
{% endhighlight %}

### Description

Return the dimension of `geom`:

* 0 for `POINT`s
* 1 for `LINESTRING`s
* 2 for `POLYGON`s
* The largest dimension of the components of a `GEOMETRYCOLLECTION`

<!-- This function does not seem to be SFS. Is it SQL-MM? -->

### Examples

{% highlight mysql %}
SELECT ST_Dimension('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: 0

SELECT ST_Dimension('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 1

SELECT ST_Dimension('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)),
                                  ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: 2

SELECT ST_Dimension('GEOMETRYCOLLECTION(
                       MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                       LINESTRING(2 6, 6 2),
                       POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 2
{% endhighlight %}

##### See also

* [`ST_CoordDim`](../ST_CoordDim), [`ST_GeometryType`](../ST_GeometryType)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_Dimension.java" target="_blank">Source code</a>
