---
layout: docs
title: ST_Extent
category: h2spatial-ext/properties
description: Return the extent of the geometry collection 
prev_section: ST_Explode
next_section: ST_XMax
permalink: /docs/dev/ST_Extent/
---
 
### Signature

{% highlight mysql %}
ST_Extent(Geometry geom)
{% endhighlight %}

### Description
Return the extent that encloses the geometry collection

### Examples

{% highlight mysql %}
select ST_Extent('MULTIPOINT(5 5, 1 2, 3 4, 99 3)'::Geometry) tableEnv from ptClouds;
--answer : POLYGON ((1 2, 1 5, 99 5, 99 2, 1 2))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_Extent.java" target="_blank">Source code</a>
