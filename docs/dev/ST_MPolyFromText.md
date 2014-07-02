---
layout: docs
title: ST_MPolyFromText
category: geom2D/geometry-conversion
is_function: true
description: Well Known Text &rarr; <code>MULTIPOLYGON</code>
prev_section: ST_MPointFromText
next_section: ST_PointFromText
permalink: /docs/dev/ST_MPolyFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_MPolyFromText(VARCHAR wkt, INT srid);
{% endhighlight %}

### Description

{% include from-wkt-desc.html type='MULTIPOLYGON' %}
{% include z-coord-warning.html %}
{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_MPolyFromText(
    'MULTIPOLYGON(((28 26, 28 0, 84 0, 84 42, 28 26),
                   (52 18, 66 23, 73 9, 48 6, 52 18)),
                  ((59 18, 67 18, 67 13, 59 13, 59 18)))', 101);
-- Answer: MULTIPOLYGON(((28 26, 28 0, 84 0, 84 42, 28 26),
--                       (52 18, 66 23, 73 9, 48 6, 52 18)),
--                      ((59 18, 67 18, 67 13, 59 13, 59 18)))

SELECT ST_MPolyFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a MULTIPOLYGON.
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_MPolyFromText.java" target="_blank">Source code</a>
