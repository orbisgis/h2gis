---
layout: docs
title: ST_Polygonize
category: h2spatial-ext/process-geometries
description: Create a <code>MULTIPOLYGON</code> from edges of Geometries
prev_section: h2spatial-ext/process-geometries
next_section: ST_PrecisionReducer
permalink: /docs/dev/ST_Polygonize/
---

### Signature

{% highlight mysql %}
MULTIPOLYGON ST_Polygonize(GEOMETRY geom);
{% endhighlight %}

### Description
Creates a `MULTIPOLYGON` containing possible Polygons formed from the constituent linework of a set of
Geometries. If the endpoints of the Geometries are not properly joined this function return
null.

### Examples

{% highlight mysql %}
SELECT ST_Polygonize('POINT (1 2)');
-- Answer: null

SELECT ST_Polygonize('MULTILINESTRING((1 2, 2 4, 5 2), 
                                      (1 4, 4 1, 4 4))')
-- Answer: null

SELECT ST_Polygonize('MULTILINESTRING((1 2, 2 4, 4 4, 5 2), 
                                      (5 2, 2 1, 2 4, 1 5))');
-- Answer: null

SELECT ST_Polygonize('LINESTRING(1 2, 2 4, 4 4, 5 2, 2 2)');
-- Answer: null
{% endhighlight %}

<img class="displayed" src="../ST_Polygonize_1.png"/>

{% highlight mysql %}
SELECT ST_Polygonize('LINESTRING(1 2, 2 4, 4 4, 5 2,1 2)');
-- Answer: MULTIPOLYGON(((1 2, 2 4, 4 4, 5 2, 1 2)))
{% endhighlight %}

<img class="displayed" src="../ST_Polygonize_2.png"/>

{% highlight mysql %}
SELECT ST_Polygonize('MULTILINESTRING((1 2, 2 4, 4 4, 5 2), 
                                      (5 2, 2 1, 1 2))');
-- Answer: MULTIPOLYGON(((1 2, 2 4, 4 4, 5 2, 2 1, 1 2)))
{% endhighlight %}

<img class="displayed" src="../ST_Polygonize_3.png"/>

{% highlight mysql %}
SELECT ST_Polygonize('POLYGON((2 2, 2 4, 5 4, 5 2, 2 2))');
-- Answer: MULTIPOLYGON((2 2, 2 4, 5 4, 5 2, 2 2))

SELECT ST_Polygonize(st_union('MULTILINESTRING((1 2, 2 4, 5 2), 
                                                (1 4, 4 1, 4 4))'));
-- Answer: MULTIPOLYGON (((4 2.6666666666666665, 4 1, 
--                         1.6666666666666667 3.3333333333333335, 
--                         2 4, 4 2.6666666666666665)))

{% endhighlight %}

<img class="displayed" src="../ST_Polygonize_4.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/processing/ST_Polygonize.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
