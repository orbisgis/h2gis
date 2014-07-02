---
layout: docs
title: ST_IsClosed
category: geom2D/properties
is_function: true
description: Return true if a Geometry is a closed `LINESTRING` or `MULTILINESTRING`
prev_section: ST_InteriorRingN
next_section: ST_IsEmpty
permalink: /docs/dev/ST_IsClosed/
---

### Signature

{% highlight mysql %}
BOOLEAN ST_IsClosed(GEOMETRY geom);
{% endhighlight %}

### Description

Returns `TRUE` if `geom` is a closed `LINESTRING` or `MULTILINESTRING`, null
otherwise. A `MULTILINESTRING` is closed if all its `LINESTRING`s are closed.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_IsClosed('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: FALSE

SELECT ST_IsClosed('LINESTRING(2 1, 1 3, 5 2, 2 1)');
-- Answer: TRUE
{% endhighlight %}

<img class="displayed" src="../ST_IsClosed.png"/>

{% highlight mysql %}
SELECT ST_IsClosed('MULTILINESTRING((0 2, 3 2, 3 6, 0 6, 0 1),
                                    (5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: FALSE

SELECT ST_IsClosed('MULTILINESTRING((0 2, 3 2, 3 6, 0 6, 0 2),
                                    (5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: TRUE
{% endhighlight %}

##### See also

* [`ST_IsRing`](../ST_IsRing)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_IsClosed.java" target="_blank">Source code</a>
