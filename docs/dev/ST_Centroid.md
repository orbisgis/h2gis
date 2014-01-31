---
layout: docs
title: ST_Centroid
category: h2spatial/properties
description: Compute the centroid of a Geometry
prev_section: ST_Boundary
next_section: ST_Dimension
permalink: /docs/dev/ST_Centroid/
---

### Signatures

{% highlight mysql %}
POINT ST_Centroid(GEOMETRY geom);
{% endhighlight %}

### Description

Computes the geometric center of a `GEOMETRY`.

<div class="note warning">

<h5>In the case of <code>GEOMETRYCOLLECTION</code> which contents <code>POLYGON</code> or <code>MULTIPOLYGON</code> and other Geometry, <code>ST_Centroid</code> return the centroid of the <code>POLYGON</code> and take not account of <code>LINESTRING</code> and <code>POINT</code>.</h5>

</div>

 
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Centroid('POINT(4 4)');
-- Answer: POINT(4 4)

SELECT ST_Centroid('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: POINT(1.25 2.25)
{% endhighlight %}

<img class="displayed" src="../ST_Centroid_1.png"/>

{% highlight mysql %}
SELECT ST_Centroid('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: POINT(2.472556942838389 2.3241856476127962)

SELECT ST_Centroid('MULTILINESTRING((1 5, 6 5), (5 1, 5 4))');
-- Answer: POINT(4.0625 4.0625)
{% endhighlight %}

<img class="displayed" src="../ST_Centroid_2.png"/>

{% highlight mysql %}
SELECT ST_Centroid('POLYGON((1 5, 1 2, 6 2, 3 3, 3 4, 5 6, 1 5))');
-- Answer: POINT(2.5964912280701755 3.666666666666667)

SELECT ST_Centroid('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)), 
                    ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: POINT(2.142857142857143 3.5)
{% endhighlight %}

<img class="displayed" src="../ST_Centroid_3.png"/>

{% highlight mysql %}
SELECT ST_Centroid('GEOMETRYCOLLECTION(
                    POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)), 
                    LINESTRING(2 6, 6 2), 
                    MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: POINT(2.5 4)
{% endhighlight %}

<img class="displayed" src="../ST_Centroid_4.png"/>


##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Centroid.java" target="_blank">Source code</a>
