---
layout: docs
title: ST_Explode
category: h2spatial-ext/properties
description: Return the multiple geometries to one geometry collection
prev_section: ST_CoordDim
next_section: ST_Extent
permalink: /docs/dev/ST_Explode/
---
 
### Signature

{% highlight mysql %}
tableName [*,explod_id]  	ST_Explode('tableName')
{% endhighlight %}

### Description
This table function explode Geometry Collection into multiple geometries

### Examples

{% highlight mysql %}
create table test_point as select 'MULTIPOINT((1 1), (2 2))'::Geometry the_geom;
select the_geom::Geometry, explod_id  from st_explode('test_point');
-- Answer:
	|CAST(THE_GEOM AS GEOMETRY)| EXPLOD_ID  |
	|--------------------------|------------|
	|	POINT (1 1)	   |	1       |
	|--------------------------|------------|
	|	POINT (2 2)	   |	2	|
	|---------------------------------------|
{% endhighlight %}
<img class="displayed" src="../ST_Explode.png"/>


##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_Explode.java" target="_blank">Source code</a>
