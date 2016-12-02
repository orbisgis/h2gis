---
layout: docs
title: ST_LocateAlong
category: geom2D/distance-functions
is_function: true
description: Return a <code>MULTIPOINT</code> containing points along the line segments of a Geometry at a given segment length fraction and offset distance
prev_section: ST_Length
next_section: ST_LongestLine
permalink: /docs/1.3/ST_LocateAlong/
---

### Signature

{% highlight mysql %}
MULTIPOINT ST_LocateAlong(GEOMETRY geom,
                          DOUBLE segmentLengthFraction,
                          DOUBLE offsetDistance);
{% endhighlight %}

### Description

Places points along the line segments composing `geom` at a distance of
`segmentLengthFraction` along the segment and at an offset distance of
`offsetDistance`. Returns them as a `MULTIPOINT`.

<div class="note">
  <h5>What about orientation?</h5>
  <p>Line segment orientation is determined by the order of the coordinates. A
  positive offset places the point to the left of the segment; a negative
  offset to the right.</p>
</div>

<div class="note warning">
  <h5>Only exterior rings are supported for <code>POLYGON</code>s.</h5>
</div>

### Examples

{% highlight mysql %}
SELECT ST_LocateAlong('LINESTRING(1 1, 5 4)', 0.5, 2);
-- Answer: MULTIPOINT((1.8 4.1))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_0.png"/>

{% highlight mysql %}
SELECT ST_LocateAlong('LINESTRING(1 1, 5 1, 5 3)', 0.5, 1);
-- Answer: MULTIPOINT((3 2), (4 2))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_1.png"/>

{% highlight mysql %}
SELECT ST_LocateAlong('POLYGON((1 1, 4 1, 4 3, 1 3, 1 1))', 0.5, -1);
-- Answer: MULTIPOINT((2.5 0), (5 2), (2.5 4), (0 2))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_2.png"/>

{% highlight mysql %}
SELECT ST_LocateAlong('GEOMETRYCOLLECTION(
                           LINESTRING(1 4, 5 4, 5 2),
                           POLYGON((1 1, 4 1, 4 3, 1 3, 1 1)))',
                      2, 1);
-- Answer: MULTIPOINT((2 -1), (-2 2), (6 0), (9 5), (7 2), (3 5))
{% endhighlight %}

<img class="displayed" src="../ST_LocateAlong_3.png"/>

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/spatial/distance/ST_LocateAlong.java" target="_blank">Source code</a>
