---
layout: docs
title: ST_IsRing
category: geom2D/properties
description: Return true if a <code>LINESTRING</code> is ring
prev_section: ST_IsRectangle
next_section: ST_IsSimple
permalink: /docs/dev/ST_IsRing/
---

### Signature

{% highlight mysql %}
boolean ST_IsRing(GEOMETRY geom);
{% endhighlight %}

### Description

Return `TRUE` if `geom` is a closed and simple `LINESTRING` or
`MULTILINESTRING`; `NULL` otherwise.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_IsRing('LINESTRING(2 1, 1 3, 6 6, 2 1)');
-- Answer: TRUE

SELECT ST_IsRing('LINESTRING(2 1, 1 3, 6 6)');
-- Answer: FALSE

SELECT ST_IsRing('LINESTRING(2 1, 1 3, 6 6, 5 7, 5 2, 2 1)');
-- Answer: FALSE

SELECT ST_IsRing('LINESTRING(2 1, 1 3, 6 6, 5 7, 5 2)');
-- Answer: FALSE
{% endhighlight %}

<img class="displayed" src="../ST_IsRing.png"/>

{% highlight mysql %}
SELECT ST_IsRing('MULTILINESTRING((0 2, 3 2, 3 6, 0 6, 0 2), 
                                  (5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: TRUE

SELECT ST_IsRing('MULTILINESTRING((0 2, 3 2, 3 6, 0 6, 0 1), 
                                  (5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: FALSE
{% endhighlight %}

##### See also

* [`ST_IsClosed`](../ST_IsClosed), [`ST_IsSimple`](../ST_IsSimple),
  [`ST_StartPoint`](../ST_StartPoint), [`ST_EndPoint`](../ST_EndPoint)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_IsRing.java" target="_blank">Source code</a>
