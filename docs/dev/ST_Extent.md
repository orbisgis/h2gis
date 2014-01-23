---
layout: docs
title: ST_Extent
category: h2spatial-ext/properties
description: Return the extent of the <CODE>GEOMETRYCOLLECTION</CODE>
prev_section: ST_Explode
next_section: ST_XMax
permalink: /docs/dev/ST_Extent/
---
 
### Signature

{% highlight mysql %}
ST_Extent(Geometry geom)
{% endhighlight %}

### Description
Return the extent that encloses the `GEOMETRYCOLLECTION`

### Examples

{% highlight mysql %}
SELECT ST_Extent('MULTIPOINT(5 5, 1 2, 3 4, 10 3)'::Geometry) 
tableEnv;
-- Answer : POLYGON ((1 2, 1 5, 10 5, 10 2, 1 2))
{% endhighlight %}
<img class="displayed" src="../ST_Extent.png"/>

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_Extent.java" target="_blank">Source code</a>
