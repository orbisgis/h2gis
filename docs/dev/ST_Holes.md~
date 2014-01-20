---
layout: docs
title: ST_Holes
category: h2spatial-ext/geometry-conversion
prev_section: h2spatial-ext/geometry-conversion
next_section: ST_ToMultiLine
permalink: /docs/dev/ST_Holes/
---

### Signature

{% highlight mysql %}
GeometryCollection ST_Holes(Geometry geom)
{% endhighlight %}

### Description

Returns the given `Geometry` or `GeometryCollection`'s holes as a
`GeometryCollection`. Returns `GEOMETRYCOLLECTION EMPTY` for geometries of
dimension less than 2.

### Examples

{% highlight mysql %}
SELECT ST_Holes('LINESTRING EMPTY'::Geometry);
-- Answer: GEOMETRYCOLLECTION EMPTY

SELECT ST_Holes('LINESTRING(5 5, 1 2, 3 4, 99 3)'::Geometry);
-- Answer: GEOMETRYCOLLECTION EMPTY

SELECT ST_Holes('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))'::Geometry);
-- Answer: GEOMETRYCOLLECTION EMPTY

SELECT ST_Holes('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),
                          (1 1, 2 1, 2 4, 1 4, 1 1))'::Geometry);
-- Answer: GEOMETRYCOLLECTION(POLYGON((1 1, 2 1, 2 4, 1 4, 1 1)))
{% endhighlight %}

<img class="displayed" src="../ST_Holes_1.png"/>

{% highlight mysql %}
SELECT ST_Holes('GEOMETRYCOLLECTION(
                POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),
                         (1 1, 2 1, 2 4, 1 4, 1 1)),
                POLYGON ((11 6, 14 6, 14 9, 11 9, 11 6),
                         (12 7, 14 7, 14 8, 12 8, 12 7)))'::Geometry);
-- Answer: GEOMETRYCOLLECTION(POLYGON((1 1, 2 1, 2 4, 1 4, 1 1)),
--                            POLYGON((12 7, 14 7, 14 8, 12 8, 12 7)))
{% endhighlight %}

<img class="displayed" src="../ST_Holes_2.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/convert/ST_Holes.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/52" target="_blank">#52</a>
