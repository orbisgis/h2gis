---
layout: docs
title: ST_SRID
category: geom2D/properties
is_function: true
description: Return a SRID value
prev_section: ST_PointOnSurface
next_section: ST_StartPoint
permalink: /docs/1.3/ST_SRID/
---

### Signature

{% highlight mysql %}
INT ST_SRID(GEOMETRY geom);
{% endhighlight %}

### Description

Returns SRID value or 0 if input Geometry does not have one.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_SRID(ST_GeomFromText('POINT(15 25)', 2154));
-- Answer: 2154

SELECT ST_SRID(ST_GeomFromText('LINESTRING(2 1, 1 3, 5 2, 2 1)',
               4326));
-- Answer: 4326
{% endhighlight %}

##### See also

* [`ST_SetSRID`](../ST_SetSRID),
[`ST_GeomFromText`](../ST_GeomFromText)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_SRID.java" target="_blank">Source code</a>