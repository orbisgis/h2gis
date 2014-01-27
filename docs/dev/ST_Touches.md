---
layout: docs
title: ST_Touches
category: h2spatial/predicates
description: Return true if Geometry A touches Geometry B
prev_section: ST_Relate
next_section: ST_Within
permalink: /docs/dev/ST_Touches/
---

### Signatures

{% highlight mysql %}
boolean ST_Touches(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns true if `geomA` touches `geomB`.

Touches means that:
  * `geomA` and `geomB` have at least one point in common.
  * The interiors of `geomA` and `geomB` do not intersect.

##### Remarks
  * `ST_Touches` can only be used to determine the relation between pairs listed here : `(POLYGON, POLYGON)`, `(POLYGON, LINESTRING)`, `(POLYGON, POINT)`, `(LINESTRING, LINESTRING)` and `(LINESTRING, POINT)`. The relation `(POINT, POINT)` is excluded. 
  * `GEOMETRYCOLLECTION`s are not taken into account.

{% include sfs-1-2-1.html %}

### Examples

##### Cases where `ST_Touches` is true
 
{% highlight mysql %}
SELECT ST_Touches(geomA, geomB) FROM input_table;
-- Answer:    True
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POLYGON ((4 2, 7 2, 7 6, 4 6, 4 2)) |

<img class="displayed" src="../ST_Touches_1.png"/>

| geomA POLYGON | geomB LINESTRING |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | LINESTRING (2 5, 7 5) |

<img class="displayed" src="../ST_Touches_2.png"/>

| geomA POLYGON | geomB POINT |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POINT (4 3) |

<img class="displayed" src="../ST_Touches_3.png"/>

| geomA LINESTRING | geomB LINESTRING |
| ----|---- |
| LINESTRING (2 1, 5 3, 2 6) | LINESTRING (1 3, 3 5) |

<img class="displayed" src="../ST_Touches_4.png"/>

| geomA LINESTRING | geomB POINT |
| ----|---- |
| LINESTRING (2 1, 5 3, 2 6) | POINT (2 6) |

<img class="displayed" src="../ST_Touches_5.png"/>

| geomA POLYGON | geomB MULTIPOLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | MULTIPOLYGON (((4 2, 7 2, 7 6, 4 6, 4 2)), ((0 6, 1 6, 1 7, 0 7, 0 6))) |

<img class="displayed" src="../ST_Touches_6.png"/>

| geomA POLYGON | geomB MULTILINESTRING |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | MULTILINESTRING ((2 5, 7 5), (6 1, 6 4)) |

<img class="displayed" src="../ST_Touches_7.png"/>

| geomA POLYGON | geomB MULTIPOINT |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | MULTIPOINT ((4 3), (6 2)) |

<img class="displayed" src="../ST_Touches_8.png"/>

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POLYGON ((4 5, 7 5, 7 6, 4 6, 4 5)) |

<img class="displayed" src="../ST_Touches_9.png"/>

##### Cases where `ST_Touches` is false

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POLYGON ((3 4, 7 4, 7 6, 3 6, 3 4)) |

<img class="displayed" src="../ST_Touches_10.png"/>

##### See also

* [`ST_Intersects`](../ST_Intersects)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Touches.java" target="_blank">Source code</a>
