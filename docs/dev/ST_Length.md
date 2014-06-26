---
layout: docs
title: ST_Length
category: geom2D/distance-functions
description: Return the length of a Geometry
prev_section: ST_FurthestCoordinate
next_section: ST_LocateAlong
permalink: /docs/dev/ST_Length/
---

### Signature

{% highlight mysql %}
double ST_Length(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the length of `geom`:
* Linear geometries return their length.
* Areal geometries return their perimeter.
* Others return 0.0.

Length is measured in the units of the spatial reference system.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Length('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: 0.0

SELECT ST_Length('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 6.35917360311745

SELECT ST_Length('POLYGON((1 2, 4 2, 4 6, 1 6, 1 2))');
-- Answer: 14.0

SELECT ST_Length('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)), 
                               ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: 20.0

SELECT ST_Length('GEOMETRYCOLLECTION(
                    MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
                    LINESTRING(2 1, 1 3, 5 2), 
                    POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 20.35917360311745
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Length.java" target="_blank">Source code</a>
