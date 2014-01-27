---
layout: docs
title: ST_Extent
category: h2spatial-ext/properties
description: Return the minimum bounding box of the <code>GEOMETRYCOLLECTION</code>
prev_section: ST_Explode
next_section: ST_XMax
permalink: /docs/dev/ST_Extent/
---
 
### Signature

{% highlight mysql %}
GEOMETRY ST_Extent(GEOMETRY geom);
{% endhighlight %}

### Description
Returns the minimum bounding box that encloses the `GEOMETRYCOLLECTION`.

### Examples

{% highlight mysql %}
SELECT ST_Extent('MULTIPOINT((5 6), (1 2), (3 4), (10 3))'::Geometry);
-- Answer: POLYGON ((1 2, 1 6, 10 6, 10 2, 1 2))
{% endhighlight %}
<img class="displayed" src="../ST_Extent.png"/>

##### See also
* <a href="http://www.h2gis.org/docs/dev/ST_Envelope/" target="_blank">ST_Envelope</a>
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_Extent.java" target="_blank">Source code</a>
