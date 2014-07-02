---
layout: docs
title: ST_Interpolate3DLine
category: geom3D/edit-geometries
is_function: true
description: Return a Geometry with a interpolation of z values.
prev_section: ST_AddZ
next_section: ST_MultiplyZ
permalink: /docs/dev/ST_Interpolate3DLine/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_Interpolate3DLine(GEOMETRY geom);
{% endhighlight %}

### Description

Interpolate the *z*-values of `geom` based on the *z*-values of its
first and last coordinates.
Does an interpolation on each indiviual Geometry of `geom` if it is
a `GEOMETRYCOLLECTION`.

Returns `geom` untouched if its first or last coordinate has no
*z*-value.

{% include other-line-multiline.html %}

### Examples

{% highlight mysql %}
SELECT ST_Interpolate3DLine('LINESTRING(0 0 1, 5 0, 10 0 10)');
-- Answer:                   LINESTRING(0 0 1, 5 0 5.5, 10 0 10)

SELECT ST_Interpolate3DLine(
          'MULTILINESTRING((0 0 0, 5 0, 10 0 10),
                           (0 0 0, 50 0, 100 0 100))');
-- Answer: MULTILINESTRING((0 0 0, 5 0 5, 10 0 10),
--                         (0 0 0, 50 0 50, 100 0 100))
{% endhighlight %}

###### Nonexamples

{% highlight mysql %}
-- Returns the Geometry untouched:
SELECT ST_Interpolate3DLine('LINESTRING(0 8, 1 8, 3 8)');
-- Answer: LINESTRING(0 8, 1 8, 3 8)

-- Returns NULL for Geometries other than LINESTRINGs and
-- MULTILINESTRINGs:
SELECT ST_Interpolate3DLine(
            'POLYGON((2 0 1, 2 8 0, 4 8, 4 0, 2 0))');
-- Answer: NULL
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_Interpolate3DLine.java" target="_blank">Source code</a>
