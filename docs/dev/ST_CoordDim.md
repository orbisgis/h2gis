---
layout: docs
title: ST_CoordDim
category: h2spatial-ext/properties
prev_section: ST_CompactnessRatio
next_section: ST_Explode
permalink: /docs/dev/ST_CoordDim/
---
 
### Signature

{% highlight mysql %}
int ST_CoordDim(Geometry geom)
{% endhighlight %}

### Description
Returns the dimension of the coordinates of `geom`.

### Examples

{% highlight mysql %}
SELECT ST_CoordDim('POINT(1 2)'::Geometry);

-- Answer: 2

SELECT ST_CoordDim('LINESTRING(0 0 0, 1 1 2)'::Geometry);
-- Answer: 3
{% endhighlight %}

<img class="displayed" src="../ST_CoordDim.png"/>

{% highlight mysql %}
SELECT ST_CoordDim('LINESTRING (1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)'::Geometry);
-- Answer: 3

SELECT ST_CoordDim('MULTIPOLYGON (((0 0, 1 1, 0 1, 0 0)))'::Geometry);
-- Answer: 2

{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_CoordDim.java" target="_blank">Source code</a>
