---
layout: docs
title: ST_Relate
category: h2spatial/predicates
description: Return the <code>IntersectionMatrix code</code> for the two Geometries or true if the given <code>IntersectionMatrix code</code>  match the elements in intersectionPattern.
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

Computes the relation between two Geometries, as described in the SFS specification. It can be used in two ways: 
* First, if only two Geometries are given, it returns a 9-character string representation that refers to the corresponding IntersectionMatrix (DE-9IM). 
* Secondly, if two Geometries and an 9-character string representation (`iMatrix`) are given, it returns true if the computed `iMatrix` match with the given one. If no match, false is returned.

<div class="note"><p>See <a href="http://en.wikipedia.org/wiki/DE-9IM">here</a> for more information about the DE9-IM.</p></div>

{% include type-warning.html type='GEOMETRYCOLLECTION' %}

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Relate('LINESTRING(1 2, 3 4)', 
                 'LINESTRING(5 6, 7 3)');
-- Answer: FF1FF0102
{% endhighlight %}

<img class="displayed" src="../ST_Relate_1.png"/>

{% highlight mysql %}
SELECT ST_Relate('POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))', 
                 'POLYGON((4 2, 7 2, 7 6, 4 6, 4 2))');
-- Answer: FF2F11212

SELECT ST_Relate('POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))', 
                 'POLYGON((3 2, 6 2, 6 6, 3 6, 3 2))');
-- Answer: 212101212
{% endhighlight %}

<img class="displayed" src="../ST_Relate_2.png"/>

{% highlight mysql %}
SELECT ST_Relate('POINT(1 2)', ST_Buffer('POINT(1 2)',2), 
           '0F*FFF212');
-- Answer: TRUE
Note: * = all values are accepted.

SELECT ST_Relate('POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))', 
                 'POLYGON((4 2, 7 2, 7 6, 4 6, 4 2))', 
           '0F1FFF212');
-- Answer: FALSE
{% endhighlight %}

##### See also

* [`ST_Contains`](../ST_Contains), [`ST_Covers`](../ST_Covers), [`ST_Crosses`](../ST_Crosses),   [`ST_Disjoint`](../ST_Disjoint),
[`ST_Equals`](../ST_Equals),[`ST_Intersects`](../ST_Intersects),
[`ST_Overlaps`](../ST_Overlaps),[`ST_Touches`](../ST_Touches),
[`ST_Within`](../ST_Within),
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Relate.java" target="_blank">Source code</a>
