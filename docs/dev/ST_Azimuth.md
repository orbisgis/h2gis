---
layout: docs
title: ST_Azimuth
category: geom2D/trigonometry
is_function: true
description: Return the azimuth of the segment from point A to point B
prev_section: geom2D/trigonometry
next_section: geom3D
permalink: /docs/dev/ST_Azimuth/
---

### Signature

{% highlight mysql %}
DOUBLE ST_Azimuth(GEOMETRY pointA, GEOMETRY pointB);
{% endhighlight %}

### Description

Returns the [azimuth][wiki] in radians of the segment from `pointA` to
`pointB` clockwise from the North (0, 1).
Returns `NULL` if `pointA` and `pointB` are not `POINT` Geometries
or are coincident.

### Examples

{% highlight mysql %}
SELECT ST_Azimuth('LINESTRING(0 0, 5 5)', 'POINT(10 0)');
-- Answer: NULL

SELECT DEGREES(ST_Azimuth('POINT(0 1)', 'POINT(10 1)'));
-- Answer: 90.0

SELECT ST_Azimuth('POINT(0 1)', 'POINT(10 1)');
-- Answer: 1.5707963267948966
{% endhighlight %}

<img class="displayed" src="../ST_Azimuth_1.png"/>

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/trigonometry/ST_Azimuth.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Azimuth
