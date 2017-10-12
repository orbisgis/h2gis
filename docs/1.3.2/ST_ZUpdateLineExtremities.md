---
layout: docs
title: ST_ZUpdateLineExtremities
category: geom3D/edit-geometries
is_function: true
description: Update the start and end <i>z</i>-values of a Geometry
prev_section: ST_UpdateZ
next_section: geom3D/distance-functions
permalink: /docs/1.3.2/ST_ZUpdateLineExtremities/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_ZUpdateLineExtremities(GEOMETRY geom, DOUBLE startZ,
                                   DOUBLE endZ);
GEOMETRY ST_ZUpdateLineExtremities(GEOMETRY geom, DOUBLE startZ,
                                   DOUBLE endZ, BOOLEAN interpolate);
{% endhighlight %}

### Description

Replaces the first *z*-value of `geom` by `startZ` and the last
*z*-value of `geom` by `endZ`, and optionally interpolates
intermediate coordinates relative to segment length according to the
value of `interpolate`:

| Value            | Meaning                                   |
|------------------|-------------------------------------------|
| `TRUE` (default) | Interpolate intermediate coordinates      |
| `FALSE`          | Update only the first and last *z*-values |

{% include other-line-multiline.html %}

### Examples

{% highlight mysql %}
-- Here there is no interpolation since this is a LINESTRING between
-- two points:
SELECT ST_ZUpdateLineExtremities(
            'LINESTRING(250 250, 280 290)', 40, 10);
-- Answer:   LINESTRING(250 250 40, 280 290 10)

-- Each component LINESTRING is interpolated individually:
SELECT ST_ZUpdateLineExtremities(
            'MULTILINESTRING((1 1 1, 1 6 2, 2 2 1, -1 2 3),
                             (1 2 0, 4 2, 4 6 2))', 0, 10);
-- Answer:   MULTILINESTRING((1 1 0, 1 6 3.6889, 2 2 2.4746, -1 2 10),
--                           (1 2 0, 4 2 5.7142, 4 6 10))

-- The following two examples give the same result since the boolean
-- "interpolate" is TRUE by default:
SELECT ST_ZUpdateLineExtremities(
            'LINESTRING(0 0, 5 0 1, 15 0)', 0, 20);
-- Answer:   LINESTRING(0 0 0, 5 0 13.333333333333332, 15 0 20)
SELECT ST_ZUpdateLineExtremities(
            'LINESTRING(0 0, 5 0 1, 15 0)', 0, 20, 'true');
-- Answer:   LINESTRING(0 0 0, 5 0 13.333333333333332, 15 0 20)

-- If we set it to false, intermediate z-values are not updated:
SELECT ST_ZUpdateLineExtremities(
            'LINESTRING(0 0, 5 0 1, 10 0)', 0, 20, 'false');
-- Answer:   LINESTRING(0 0 0, 5 0 1, 10 0 20)
{% endhighlight %}

##### Non-examples

{% highlight mysql %}
-- Returns NULL for Geometries other than LINESTRINGs and
-- MULTILINESTRINGs:
SELECT ST_ZUpdateLineExtremities(
            'POLYGON((1 1, 1 7, 7 7 -1, 7 1 -1, 1 1))', 10, 15);
-- Answer: NULL
{% endhighlight %}

##### See also
* [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_ZUpdateLineExtremities.java" target="_blank">Source code</a>
