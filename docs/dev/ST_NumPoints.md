---
layout: docs
title: ST_NumPoints
category: geom2D/properties
is_function: true
description: Return the number of points in a Geometry
prev_section: ST_NumInteriorRings
next_section: ST_PointN
permalink: /docs/dev/ST_NumPoints/
---

### Signature

{% highlight mysql %}
int ST_NumPoints(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the number of points in `geom`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_NumPoints('POINT (2 2)');
-- Answer: 1

SELECT ST_NumPoints('MULTIPOINT(2 2, 4 4)');
-- Answer: 2

-- ST_NumPoints includes duplicate points in the count.
SELECT ST_NumPoints('MULTIPOINT(2 2, 4 4, 4 4)');
-- Answer: 3

SELECT ST_NumPoints('MULTILINESTRING((2 2, 4 4), (3 1, 6 3))');
-- Answer: 4

SELECT ST_NumPoints('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                             (1 1, 2 1, 2 5, 1 5, 1 1), 
                             (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: 15
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_NumPoints.java" target="_blank">Source code</a>
