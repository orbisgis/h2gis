---
layout: docs
title: ST_LongestLine
category: geom2D/distance-functions
is_function: true
description: Returns the 2-dimensional longest <code>LINESTRING</code> between the points of two geometries
prev_section: ST_LocateAlong
next_section: ST_MaxDistance
permalink: /docs/1.3.2/ST_LongestLine/
---

### Signature

{% highlight mysql %}
LINESTRING ST_LongestLine(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns the 2-dimensional longest `LINESTRING` between the points of two geometries (`geomA` and `geomB`).

If `geomA` and `geomB` are the same, the function will return the longest `LINESTRING` between the two farthest vertices in this geometry.

### Examples

##### Cases where `geomA` and `geomB` are different

{% highlight mysql %}
SELECT ST_Longestline('POLYGON ((0 1, 1 1, 1 0, 0 0, 0 1))', 
                      'POLYGON ((2 3, 2 2, 3 2, 4 3, 2 3))');
-- Answer: LINESTRING (0 0, 4 3)
{% endhighlight %}

<img class="displayed" src="../ST_LongestLine_1.png"/>

{% highlight mysql %}
SELECT ST_Longestline('POLYGON ((0 1, 1 1, 1 0, 0 0, 0 1))', 
		      'LINESTRING (1 3, 4 3)');
-- Answer: LINESTRING (0 0, 4 3)
{% endhighlight %}

<img class="displayed" src="../ST_LongestLine_2.png"/>

{% highlight mysql %}
SELECT ST_Longestline('POLYGON ((0 1, 1 1, 1 0, 0 0, 0 1))', 
		      'POINT (3 2)');
-- Answer: LINESTRING (0 0, 3 2)
{% endhighlight %}

<img class="displayed" src="../ST_LongestLine_3.png"/>

{% highlight mysql %}
SELECT ST_Longestline('MULTIPOLYGON (((0 1, 1 1, 1 0, 0 0, 0 1)), 
				    ((2 3, 2 2, 3 3, 2 3)))', 
		      'POINT (3 2)');
-- Answer: LINESTRING (0 0, 3 2)
{% endhighlight %}

<img class="displayed" src="../ST_LongestLine_4.png"/>

##### Case where `geomA` is equal to `geomB`

{% highlight mysql %}
SELECT ST_Longestline('POLYGON ((1 3, 0 0, 3 2, 1 3))', 
		      'POLYGON ((1 3, 0 0, 3 2, 1 3))');
-- Answer: LINESTRING (0 0, 3 2)
{% endhighlight %}

<img class="displayed" src="../ST_LongestLine_5.png"/>


##### See also

* [`ST_MaxDistance`](../ST_MaxDistance), [`ST_ClosestCoordinate`](../ST_ClosestCoordinate)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/distance/ST_LongestLine.java" target="_blank">Source code</a>
