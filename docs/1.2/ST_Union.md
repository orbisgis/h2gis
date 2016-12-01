---
layout: docs
title: ST_Union
category: geom2D/operators
is_function: true
description: Compute the union of two or more Geometries
prev_section: ST_SymDifference
next_section: geom2D/predicates
permalink: /docs/1.2/ST_Union/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Union(GEOMETRY geomA);
GEOMETRY ST_Union(GEOMETRY geomA, GEOMETRY geomB);
GEOMETRY ST_Union(GEOMETRYCOLLECTION geom);
{% endhighlight %}

### Description

`ST_Union` can be used as a scalar or an aggregate function.

##### Scalar function

Computes the union of one or more Geometries.
Input Geometries can be `(MULTI)POINT`s, `(MULTI)LINESTRING`s, `(MULTI)POLYGON`s or `GEOMETRYCOLLECTION`s.

##### Aggregate function
Computes the union of a set of Geometries.
`geom` is a `GEOMETRYCOLLECTION` resulting from an `ST_Accum` operation on a table.

##### In both cases:
  * If no input Geometriy is given, the result is `NULL`.
  * Output Geometries can be single or multiple.

{% include sfs-1-2-1.html %}

### Examples

##### Scalar function

| geomA Polygon                      | geomB Polygon                      |
|------------------------------------|------------------------------------|
| POLYGON((1 1, 7 1, 7 6, 1 6, 1 1)) | POLYGON((3 2, 8 2, 8 8, 3 8, 3 2)) |

{% highlight mysql %}
SELECT ST_Union(geomA, geomB) FROM input_table;
-- Answer: POLYGON((7 2, 7 1, 1 1, 1 6, 3 6, 3 8, 8 8, 8 2, 7 2))
{% endhighlight %}

<img class="displayed" src="../ST_Union_1.png"/>

##### Aggregate function

{% highlight mysql %}
CREATE TABLE input_table(geom POLYGON);
INSERT INTO input_table VALUES
     ('POLYGON((1 1, 7 1, 7 6, 1 6, 1 1))'),
     ('POLYGON((3 2, 8 2, 8 8, 3 8, 3 2))'),
     ('POLYGON((1 7, 2 7, 2 8, 1 8, 1 7))');
SELECT ST_Union(ST_Accum(geom)) FROM input_table;
-- Answer: MULTIPOLYGON(((7 2, 7 1, 1 1, 1 6, 3 6, 3 8, 8 8, 8 2, 7 2)),
--                       ((1 7, 2 7, 2 8, 1 8, 1 7)))
{% endhighlight %}

<img class="displayed" src="../ST_Union_2.png"/>

##### See also

* [`ST_Accum`](../ST_Accum)
* <a href="https://github.com/orbisgis/h2gis/blob/v1.2.4/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/operators/ST_Union.java" target="_blank">Source code</a>
