---
layout: docs
title: ST_GeomFromText
category: geom2D/geometry-conversion
is_function: true
description: Well Known Text &rarr; Geometry
prev_section: ST_Force2D
next_section: ST_GeometryTypeCode
permalink: /docs/dev/ST_GeomFromText/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_GeomFromText(VARCHAR wkt);
GEOMETRY ST_GeomFromText(VARCHAR wkt, INT srid);
{% endhighlight %}

### Description

Converts the Well Known Text `wkt` into a Geometry with spatial reference id
`srid`.  The default value of `srid` is 0.

{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_GeomFromText('POINT(2 3)', 27572);
-- Answer: POINT(2 3)

SELECT ST_SRID(ST_GeomFromText('LINESTRING(1 3, 1 1, 2 1)'));
-- Answer: 0

SELECT ST_GeomFromText('POLYGON((0 0 -1, 2 0 2, 2 1 3, 0 0 -1))');
-- Answer: POLYGON((0 0, 2 0, 2 1, 0 0))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_GeomFromText.java" target="_blank">Source code</a>
