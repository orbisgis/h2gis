---
layout: docs
title: ST_CoordDim
category: geom2D/properties
is_function: true
description: Return the dimension of the coordinates of a Geometry
prev_section: ST_CompactnessRatio
next_section: ST_Dimension
permalink: /docs/1.4.0/ST_CoordDim/
---

### Signature

{% highlight mysql %}
INT ST_CoordDim(GEOMETRY geom)
{% endhighlight %}

### Description
Returns the dimension of the coordinates of `geom`.

### Examples

{% highlight mysql %}
SELECT ST_CoordDim('POINT(1 2)');
-- Answer: 2

SELECT ST_CoordDim('LINESTRING(0 0 0, 1 1 2)');
-- Answer: 3
{% endhighlight %}

<img class="displayed" src="../ST_CoordDim.png"/>

{% highlight mysql %}
SELECT ST_CoordDim('LINESTRING(1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)');
-- Answer: 3

SELECT ST_CoordDim('MULTIPOLYGON(((0 0, 1 1, 0 1, 0 0)))');
-- Answer: 2

{% endhighlight %}

##### See also

* [`ST_Dimension`](../ST_Dimension)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_CoordDim.java" target="_blank">Source code</a>
