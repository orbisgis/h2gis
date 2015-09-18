---
layout: docs
title: ST_Disjoint
category: geom2D/predicates
is_function: true
description: Return true Geometries A and B are disjoint
prev_section: ST_DWithin
next_section: ST_EnvelopesIntersect
permalink: /docs/dev/ST_Disjoint/
---

### Signatures

{% highlight mysql %}
BOOLEAN ST_Disjoint(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns true if `geomA` and `geomB` are disjoint.

Disjoint means that the two geometries have no point in common.

{% include type-warning.html type='GEOMETRYCOLLECTION' %}
{% include sfs-1-2-1.html %}
{% include spatial_indice_warning.html %}

### Examples

##### Cases where `ST_Disjoint` is true

{% highlight mysql %}
SELECT ST_Disjoint(geomA, geomB) FROM input_table;
-- Answer:    TRUE
{% endhighlight %}

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | POLYGON((6 3, 7 3, 7 6, 6 6, 6 3))  |

<img class="displayed" src="../ST_Disjoint_1.png"/>

| geomA LINESTRING           | geomB LINESTRING      |
|----------------------------|-----------------------|
| LINESTRING(2 1, 5 3, 2 6)  | LINESTRING(6 2, 6 6)  |

<img class="displayed" src="../ST_Disjoint_2.png"/>

| geomA LINESTRING           | geomB POINT |
|----------------------------|-------------|
| LINESTRING(2 1, 5 3, 2 6)  | POINT(4 5)  |

<img class="displayed" src="../ST_Disjoint_3.png"/>

##### Cases where `ST_Disjoint` is false

{% highlight mysql %}
SELECT ST_Disjoint(geomA, geomB) FROM input_table;
-- Answer:    FALSE
{% endhighlight %}

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | POLYGON((3 2, 6 2, 6 6, 3 6, 3 2))  |

<img class="displayed" src="../ST_Disjoint_4.png"/>

| geomA POLYGON                       | geomB MULTIPOLYGON                                                      |
|-------------------------------------|-------------------------------------------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | MULTIPOLYGON(((4 2, 7 2, 7 6, 4 6, 4 2)), ((0 6, 1 6, 1 7, 0 7, 0 6)))  |

<img class="displayed" src="../ST_Disjoint_5.png"/>

##### See also

* [`ST_Contains`](../ST_Contains), [`ST_Overlaps`](../ST_Overlaps), [`ST_Touches`](../ST_Touches)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Disjoint.java" target="_blank">Source code</a>
