---
layout: docs
title: ST_ClosestCoordinate
category: geom2D/distance-functions
is_function: true
description: Return the coordinate(s) of a Geometry closest to a <code>POINT</code>
prev_section: ST_Area
next_section: ST_ClosestPoint
permalink: /docs/1.3/ST_ClosestCoordinate/
---

### Signatures

{% highlight mysql %}
{POINT, MULTIPOINT} ST_ClosestCoordinate(POINT point, GEOMETRY geom);
{% endhighlight %}

### Description

Returns the coordinate of `geom` closest to `point` using 2D distances
(z-coordinates are ignored).

{% include uniqueness.html rel='closest' type='coordinate' %}

### Examples

{% highlight mysql %}
SELECT ST_ClosestCoordinate('POINT(0 0)',
                            'POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))');
-- Answer: POINT(2 2)
{% endhighlight %}

<img class="displayed" src="../ST_ClosestCoordinate_1.png"/>

{% highlight mysql %}
SELECT ST_ClosestCoordinate('POINT(4 2.5)',
                            'POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))');
-- Answer: MULTIPOINT((0 0), (0 5))
{% endhighlight %}

<img class="displayed" src="../ST_ClosestCoordinate_2.png"/>

{% highlight mysql %}
SELECT ST_ClosestCoordinate('POINT(4 2)',
                            'LINESTRING(10 0, 10 5, 0 5)');
-- Answer: POINT(0 5)
{% endhighlight %}

<img class="displayed" src="../ST_ClosestCoordinate_3.png"/>

{% highlight mysql %}
CREATE TABLE input_table(point POINT);
INSERT INTO input_table VALUES
    ('POINT(0 0)'),
    ('POINT(4 2.5)'),
    ('POINT(5 2.5)'),
    ('POINT(6 2.5)'),
    ('POINT(5 7)');
SELECT ST_ClosestCoordinate(point,
    'POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))') CLCOORD FROM input_table;
-- Answer:
--    |                 CLCOORD                   |
--    |-------------------------------------------|
--    | POINT(0 0)                                |
--    | MULTIPOINT((0 0), (0 5))                  |
--    | MULTIPOINT((0 0), (10 0), (10 5), (0 5))  |
--    | MULTIPOINT((10 0), (10 5))                |
--    | MULTIPOINT((0 5), (10 5))                 |
{% endhighlight %}

##### See also

* [`ST_ClosestPoint`](../ST_ClosestPoint), [`ST_FurthestCoordinate`](../ST_FurthestCoordinate)
* <a href="https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/spatial/distance/ST_ClosestCoordinate.java" target="_blank">Source code</a>
