---
layout: docs
title: ST_EnvelopesIntersect
category: geom2D/predicates
is_function: true
description: Return true if the envelope of Geometry A intersects the envelope of Geometry B
prev_section: ST_Disjoint
next_section: ST_Equals
permalink: /docs/dev/ST_EnvelopesIntersect/
---

### Signatures

{% highlight mysql %}
boolean ST_EnvelopesIntersect(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns true if the envelope of `geomA` intersects the envelope of `geomA`.

As a consequence, if `ST_Intersects(geomA, geomB)` is true, then
`ST_EnvelopesIntersect(geomA, geomB)` is true.

<!-- This function does not seem to be SFS. Is it SQL-MM? -->

### Examples

##### Cases where `ST_EnvelopesIntersect` is true
 
{% highlight mysql %}
SELECT ST_EnvelopesIntersect(geomA, geomB) FROM input_table;
-- Answer:    True
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((3 1, 3 4, 5 7, 1 5, 3 1)) | POLYGON ((7 2, 7 6, 4 4, 7 2)) |

<img class="displayed" src="../ST_EnvelopesIntersect_1.png"/>

| geomA LINESTRING | geomB LINESTRING |
| ----|---- |
| LINESTRING (2 2, 6 3, 3 6) | LINESTRING (1 4, 2 5, 4 3) |

<img class="displayed" src="../ST_EnvelopesIntersect_2.png"/>

| geomA MULTIPOINT | geomB MULTIPOINT |
| ----|---- |
| MULTIPOINT ((1 4), (3 5), (5 2)) | MULTIPOINT ((3 3), (4 4), (6 6)) |

<img class="displayed" src="../ST_EnvelopesIntersect_3.png"/>

| geomA POLYGON | geomB GEOMETRYCOLLECTION |
| ----|---- |
| POLYGON ((4 2, 5 2, 5 4, 4 5, 3 4, 4 3, 4 2)) | GEOMETRYCOLLECTION (POINT (2 3), LINESTRING (6 4, 4 6), POLYGON ((2 5, 3 5, 3 6, 2 6, 2 5))) |

<img class="displayed" src="../ST_EnvelopesIntersect_4.png"/>

##### Cases where `ST_EnvelopesIntersect` is false
 
{% highlight mysql %}
SELECT ST_EnvelopesIntersect(geomA, geomB) FROM input_table;
-- Answer:    False
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((3 1, 3 4, 5 7, 1 5, 3 1)) | POLYGON ((8 2, 8 6, 6 5, 8 2)) |

<img class="displayed" src="../ST_EnvelopesIntersect_5.png"/>


##### See also

* [`ST_Intersects`](../ST_Intersects), [`ST_Envelope`](../ST_Envelope)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_EnvelopesIntersect.java" target="_blank">Source code</a>
