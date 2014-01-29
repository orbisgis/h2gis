---
layout: docs
title: ST_GeomFromText
category: h2spatial/geometry-conversion
description: Well Known Text &rarr; Geometry
prev_section: ST_AsText
next_section: ST_LineFromText
permalink: /docs/dev/ST_GeomFromText/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_GeomFromText(varchar wkt);
GEOMETRY ST_GeomFromText(varchar wkt, int srid);
{% endhighlight %}

### Description

Converts a Well Known Text parameter `WKT` into a Geometry.
If you don't specify a `SRID` the value by default is 0.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_GeomFromText(
    'POINT(584173.736059813 2594514.82833411)', 27572);
-- Answer: POINT (584173.736059813 2594514.82833411)

SELECT ST_GeomFromText(
    'MULTIPOLYGON(((0 0 0, 3 2 0, 3 2 2, 0 0 2, 0 0 0),
    (-1 1 0, -1 3 0, -1 3 4, -1 1 4, -1 1 0)))',2249);
-- Answer: MULTIPOLYGON (((0 0, 3 2, 3 2, 0 0, 0 0), 
--  (-1 1, -1 3, -1 3, -1 1, -1 1)))

SELECT ST_GeomFromText('LINESTRING(1 3, 1 1, 2 1)');
-- Answer: LINESTRING (1 3, 1 1, 2 1)

SELECT ST_GeomFromText(
    'POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))');
-- Answer: POLYGON ((1 1, 3 1, 3 2, 1 2, 1 1))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_GeomFromText.java" target="_blank">Source code</a>
