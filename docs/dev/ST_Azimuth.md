---
layout: docs
title: ST_Azimuth
category: Geometry2D/trigonometry
description: Return the azimuth of the segment defined by the given <code>POINT</code>s
prev_section: Geometry2D/trigonometry
next_section: Geometry3D/list-function-3d
permalink: /docs/dev/ST_Azimuth/
---

### Signature

{% highlight mysql %}
double ST_Azimuth(GEOMETRY PointA, GEOMETRY PointB);
{% endhighlight %}

### Description
Returns the azimuth of the segment defined by the given `POINT`s, or null if the two `POINT`s are coincident. Return value is in radians. Angle is computed clockwise from the north equals to 0.

### Examples

{% highlight mysql %}
SELECT ST_Azimuth('Linestring(0 0, 5 5)', 'Point(10 0)');
-- Answer: null

SELECT Degrees(ST_Azimuth('Point(0 1)', 'Point(10 1)'));
-- Answer: 90.0

SELECT ST_Azimuth('Point(0 1)', 'Point(10 1)');
-- Answer: 1.5707963267948966
{% endhighlight %}

<img class="displayed" src="../ST_Azimuth_1.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/51910b27b5dc2b3b4353bb43a683f8649628ea8d/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/trigonometry/ST_Azimuth.java" target="_blank">Source code</a>

