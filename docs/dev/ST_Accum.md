---
layout: docs
title: ST_Accum
category: h2spatial/aggregates
description: Construct an array of Geometries
prev_section: h2spatial/aggregates
next_section: h2spatial-ext/affine-transformations
permalink: /docs/dev/ST_Accum/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Accum(GEOMETRY geom);
{% endhighlight %}

### Description

This aggregate function converts a set of Geometries into a `GEOMETRYCOLLECTION`.
`geom` can be `(MULTI)POINT`s, `(MULTI)LINESTRING`s, `(MULTI)POLYGON`s or `GEOMETRYCOLLECTION`.

This function is the inverse of `ST_Explode`.

### Examples

{% highlight mysql %}
CREATE TABLE input_table(geom GEOMETRY);
INSERT INTO input_table VALUES
    ('POLYGON ((9 0, 9 11, 10 11, 10 0, 9 0))'),
    ('POLYGON ((1 1, 1 7, 7 7, 7 1, 1 1))'),
    ('POINT (1 1)'),
    ('POINT (2 2)');
SELECT ST_Accum(geom) FROM input_table;
-- Answer: 
--  GEOMETRYCOLLECTION (
--  POLYGON ((9 0, 9 11, 10 11, 10 0, 9 0)), 
--  POLYGON ((1 1, 1 7, 7 7, 7 1, 1 1)), 
--  POINT (1 1), POINT (2 2))
{% endhighlight %}
<img class="displayed" src="../ST_Accum.png"/>

##### See also

* [`ST_Explode`](../ST_Explode), [`ST_Union`](../ST_Union)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/aggregate/ST_Accum.java" target="_blank">Source code</a>
