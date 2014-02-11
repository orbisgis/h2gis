---
layout: docs
title: ST_Relate
category: h2spatial/predicates
description: Return a <code>DE9-IM</code> or true if the <code>DE9-IM</code> is valid.
prev_section: ST_Overlaps
next_section: ST_Touches
permalink: /docs/dev/ST_Relate/
---

### Signatures

{% highlight mysql %}
varchar ST_Relate(GEOMETRY geomA, GEOMETRY geomB);
boolean ST_Relate(GEOMETRY GeomA, GEOMETRY geomB, varchar iMatrix);
{% endhighlight %}

### Description

Computes the relation between two Geometries, as described in the SFS specification. It can be used in two ways. 
First, if it is given two `GEOMETRIES`,it returns a `9-character string representation` of the 2 Geometries IntersectionMatrix (DE-9IM). 
If it is given two `GEOMETRIES` and a IntersectionMatrix representation, it will return a boolean : true it the two `GEOMETRIES`' IntersectionMatrix match the given one, false otherwise.

<div class="note"><p>you can see <a href="http://en.wikipedia.org/wiki/DE-9IM">this documentation about
	DE9-IM</a>for more explanation</p></div>

{% include type-warning.html type='GEOMETRYCOLLECTION' %}

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Relate('LINESTRING(1 2, 3 4)', 
                 'LINESTRING(5 6, 7 8)');
-- Answer: FF1FF0102
{% endhighlight %}

<img class="displayed" src="../ST_Relate_1.png"/>

{% highlight mysql %}
SELECT ST_Relate('POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))', 
                 'POLYGON((3 2, 6 2, 6 6, 3 6, 3 2))');
-- Answer: 212101212

SELECT ST_Relate('POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))', 
                 'POLYGON((4 2, 7 2, 7 6, 4 6, 4 2))');
-- Answer: FF2F11212
{% endhighlight %}

<img class="displayed" src="../ST_Relate_2.png"/>

{% highlight mysql %}
SELECT ST_Relate('POINT(1 2)', ST_Buffer('POINT(1 2)',2), 
           '0F*FFF212');
-- Answer: TRUE

SELECT ST_Relate('POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))', 
                 'POLYGON((4 2, 7 2, 7 6, 4 6, 4 2))', 
           '0F1FFF212');
-- Answer: FALSE
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Relate.java" target="_blank">Source code</a>
