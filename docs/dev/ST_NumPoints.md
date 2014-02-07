---
layout: docs
title: ST_NumPoints
category: h2spatial/properties
description: Return the Geometry's point number
prev_section: ST_NumInteriorRings
next_section: ST_PointN
permalink: /docs/dev/ST_NumPoints/
---

### Signature

{% highlight mysql %}
int ST_NumPoints(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the Geometry's point number.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_NumPoints('POINT (2 2)');
-- Answer: 1

SELECT ST_NumPoints('MULTILINESTRING((2 2, 4 4),(3 1, 6 3))');
-- Answer: 4

SELECT ST_NumPoints('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                             (1 1, 2 1, 2 5, 1 5, 1 1), 
                             (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: 15
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_NumPoints.java" target="_blank">Source code</a>
