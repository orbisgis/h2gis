---
layout: docs
title: ST_UpdateZ
category: geom3D/edit-geometries
is_function: true
description: Update the <i>z</i>-values of a Geometry
prev_section: ST_Reverse3DLine
next_section: ST_ZUpdateLineExtremities
permalink: /docs/1.2/ST_UpdateZ/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_UpdateZ(GEOMETRY geom, DOUBLE newZ);
GEOMETRY ST_UpdateZ(GEOMETRY geom, DOUBLE newZ, INT updateCondition);
{% endhighlight %}

### Description

Replaces the *z*-values of some or all of the coordinates of `geom`
by `newZ`.
The optional parameter `updateCondition` determines which
coordinates are updated:

| Value | Meaning                                       |
|-------|-----------------------------------------------|
| 1     | all *z*-values (by default)                   |
| 2     | all *z*-values except non-existant *z*-values |
| 3     | only non-existant *z*-values                  |

### Examples

{% highlight mysql %}
-- Update all z-values by default:
SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10);
-- Answer:         MULTIPOINT((190 300 10), (10 11 10))

-- Update all z-values:
SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10, 1);
-- Answer:         MULTIPOINT((190 300 10), (10 11 10))

-- Update all z-values except non-existant ones:
SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10, 2);
-- Answer:         MULTIPOINT((190 300), (10 11 10))

-- Update only non-existant z-values:
SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10, 3);
-- Answer:         MULTIPOINT((190 300 10), (10 11 2))
{% endhighlight %}

##### See also

* [`ST_ZUpdateLineExtremities`](../ST_ZUpdateLineExtremities),
  [`ST_MultiplyZ`](../ST_MultiplyZ),
  [`ST_AddZ`](../ST_AddZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/edit/ST_UpdateZ.java" target="_blank">Source code</a>
