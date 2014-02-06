---
layout: docs
title: ST_ClosestCoordinate
category: h2spatial-ext/distance-functions
description: Compute the closest coordinate(s) contained in the given Geometry starting from the given <code>point</code>.
prev_section: h2spatial-ext/distance-functions
next_section: ST_ClosestPoint
permalink: /docs/dev/ST_ClosestCoordinate/
---

### Signatures

{% highlight mysql %}
POINT ST_ClosestCoordinate(POINT 'POINT(x y)', GEOMETRY geom);
MULTIPOINT ST_ClosestCoordinate(POINT 'POINT(x y)', GEOMETRY geom);
{% endhighlight %}

### Description
`ST_ClosestCoordinate` computes the closest coordinate(s) contained in the given Geometry starting from the given `POINT`, using the 2D distance. If the coordinate is unique, it is returned as a `POINT`. If it is not, then all closest coordinates are returned in a `MULTIPOINT`.

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
INSERT INTO input_table VALUES ('POINT(0 0)'), 
    ('POINT(4 2.5)'), ('POINT(5 2.5)'),
    ('POINT(6 2.5)'), ('POINT(5 7)');
SELECT ST_ClosestCoordinate(point, 
    'POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))') FROM input_table;
-- Answer: POINT(0 0)
--    MULTIPOINT((0 0), (0 5))
--    MULTIPOINT((0 0), (10 0), (10 5), (0 5))
--    MULTIPOINT((10 0), (10 5))
--    MULTIPOINT((0 5), (10 5))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/distance/ST_ClosestCoordinate.java" target="_blank">Source code</a>
