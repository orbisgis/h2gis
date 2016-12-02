---
layout: docs
title: ST_PointN
category: geom2D/properties
is_function: true
description: Return the <i>n</i>th point of a <code>LINESTRING</code>
prev_section: ST_NumPoints
next_section: ST_PointOnSurface
permalink: /docs/dev/ST_PointN/
---

### Signature

{% highlight mysql %}
POINT ST_PointN(GEOMETRY geometry, INT n);
{% endhighlight %}

### Description

Returns the <i>n</i>th point of `geom` if `geom` is a `LINESTRING` or a
`MULTILINESTRING` containing exactly one `LINESTRING`; `NULL` otherwise.

{% include one-to-n.html %}
{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_PointN('LINESTRING(1 1, 1 6, 2 2, -1 2))', 2);
-- Answer: POINT(1 6)

SELECT ST_PointN('MULTILINESTRING((1 1, 1 6, 2 2, -1 2))', 3);
-- Answer: POINT(2 2)

SELECT ST_PointN('MULTIPOINT(1 1, 1 6, 2 2, -1 2)', 3);
-- Answer: NULL

-- This MULTILINESTRING contains two LINESTRINGs.
SELECT ST_PointN('MULTILINESTRING((1 1, 1 6, 2 2, -1 2),
                                  (0 1, 2 4))', 3);
-- Answer: NULL

SELECT ST_PointN('LINESTRING(1 1, 1 6, 2 2, -1 2))', 0);
-- Answer: Point index out of range. Must be between 1 and
-- ST_NumPoints.
{% endhighlight %}

##### See also

* [`ST_StartPoint`](../ST_StartPoint), [`ST_EndPoint`](../ST_EndPoint)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_PointN.java" target="_blank">Source code</a>
