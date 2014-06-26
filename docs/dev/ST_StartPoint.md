---
layout: docs
title: ST_StartPoint
category: geom2D/properties
description: Return the first coordinate of a <code>LINESTRING</code>
prev_section: ST_SRID
next_section: ST_X
permalink: /docs/dev/ST_StartPoint/
---

### Signature

{% highlight mysql %}
POINT ST_StartPoint(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the first coordinate of `geom` as a `POINT`, given that `geom` is a
`LINESTRING` or a `MULTILINESTRING` containing only one `LINESTRING`. Returns
`NULL` for all other Geometries.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_StartPoint('LINESTRING(1 2, 5 3, 2 6)');
-- Answer: POINT(1 2)
{% endhighlight %}

<img class="displayed" src="../ST_StartPoint.png"/>

{% highlight mysql %}
SELECT ST_StartPoint('MULTILINESTRING((1 1, 3 2, 3 1))');
-- Answer: POINT(1 1)

SELECT ST_StartPoint('MULTILINESTRING((1 1, 3 2, 3 1),
                                      (1 2, 5 3, 2 6))');
-- Answer: NULL
{% endhighlight %}

##### See also

* [`ST_EndPoint`](../ST_EndPoint), [`ST_PointN`](../ST_PointN)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_StartPoint.java" target="_blank">Source code</a>
