---
layout: docs
title: ST_Contains
category: geom2D/predicates
is_function: true
description:
prev_section: geom2D/predicates
next_section: ST_Covers
permalink: /docs/1.2/ST_Contains/
description: Return true if Geometry A contains Geometry B
---

### Signatures

{% highlight mysql %}
BOOLEAN ST_Contains(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns true if `geomA` contains `geomB`.

{% include sfs-1-2-1.html %}
{% include spatial_indice_warning.html %}

### Examples

##### Cases where `ST_Contains` is true

{% highlight mysql %}
SELECT ST_Contains(geomA, geomB) FROM input_table;
-- Answer:    TRUE
{% endhighlight %}

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))  |

<img class="displayed" src="../ST_Contains_1.png"/>

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POLYGON((1 2, 6 2, 6 5, 1 5, 1 2))  |

<img class="displayed" src="../ST_Contains_4.png"/>

| geomA POLYGON                       | geomB LINESTRING      |
|-------------------------------------|-----------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | LINESTRING(2 6, 6 2)  |

<img class="displayed" src="../ST_Contains_2.png"/>

| geomA POLYGON                       | geomB LINESTRING           |
|-------------------------------------|----------------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | LINESTRING(1 2, 1 6, 5 2)  |

<img class="displayed" src="../ST_Contains_5.png"/>

| geomA POLYGON                       | geomB POINT |
|-------------------------------------|-------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POINT(4 4)  |

<img class="displayed" src="../ST_Contains_3.png"/>

| geomA LINESTRING           | geomB LINESTRING      |
|----------------------------|-----------------------|
| LINESTRING(2 1, 5 3, 2 6)  | LINESTRING(3 5, 5 3)  |

<img class="displayed" src="../ST_Contains_10.png"/>

| geomA LINESTRING           | geomB POINT |
|----------------------------|-------------|
| LINESTRING(2 1, 5 3, 2 6)  | POINT(4 4)  |

<img class="displayed" src="../ST_Contains_11.png"/>

##### Cases where `ST_Contains` is false

{% highlight mysql %}
SELECT ST_Contains(geomA, geomB) FROM input_table;
-- Answer:    FALSE
{% endhighlight %}

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POLYGON((0 2, 5 2, 5 5, 0 5, 0 2))  |

<img class="displayed" src="../ST_Contains_7.png"/>

| geomA POLYGON                       | geomB LINESTRING      |
|-------------------------------------|-----------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | LINESTRING(2 6, 0 8)  |

<img class="displayed" src="../ST_Contains_8.png"/>

| geomA POLYGON                       | geomB LINESTRING      |
|-------------------------------------|-----------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | LINESTRING(1 2, 1 6)  |

<img class="displayed" src="../ST_Contains_12.png"/>

| geomA POLYGON                       | geomB POINT |
|-------------------------------------|-------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POINT(8 4)  |

<img class="displayed" src="../ST_Contains_6.png"/>

| geomA POLYGON                       | geomB POINT |
|-------------------------------------|-------------|
| POLYGON((1 1, 7 1, 7 7, 1 7, 1 1))  | POINT(8 4)  |

<img class="displayed" src="../ST_Contains_9.png"/>

##### See also

* [`ST_Intersects`](../ST_Intersects), [`ST_Touches`](../ST_Touches), [`ST_Overlaps`](../ST_Overlaps)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Contains.java" target="_blank">Source code</a>
