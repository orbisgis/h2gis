---
layout: docs
title: ST_Explode
category: h2spatial-ext/properties
description: Return geometry collection to multiple geometries
prev_section: ST_CoordDim
next_section: ST_Extent
permalink: /docs/dev/ST_Explode/
---
 
### Signature

{% highlight mysql %}
tableName [*, explod_id] ST_Explode('tableName')

tableName[*, explod_id] ST_Explode('tableName', 'fieldName')
{% endhighlight %}

### Description
This table function explode `Geometry Collection` in the `fieldName` column of table
`tableName` into multiple Geometries. 
If no field name is specified, the first Geometry column is used. 

### Examples

{% highlight mysql %}
CREATE table test_point AS SELECT 'MULTIPOINT((1 1), (2 2))' the_geom;
SELECT the_geom, explod_id FROM st_explode('test_point');
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
