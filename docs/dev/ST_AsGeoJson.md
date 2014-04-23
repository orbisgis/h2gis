---
layout: docs
title: ST_AsGeoJson
category: h2drivers/GeoJson
description: Convert Geometries to a GeoJSON representation
prev_section: GeoJsonWrite
next_section: h2drivers/GPX
permalink: /docs/dev/ST_AsGeoJson/
---

### Signature

{% highlight mysql %}
Varchar ST_AsGeoJson(GEOMETRY geom);
{% endhighlight %}

### Description
Returns the Geometry as a Geometry Javascript Object Notation (GeoJSON 1.0) element. 2D and 3D Geometries are both supported. 
GeoJSON only supports SFS 1.1 geometry types (Point, Linestring,
 Polygon and Collection).

### Examples

{% highlight mysql %}
CREATE TABLE table_point(idarea int primary key, the_geom POINT);
INSERT INTO table_point VALUES(1, 'POINT(1 2)');
SELECT ST_AsGeoJSON(the_geom) FROM table_point;
-- Answer: {"type":"Point","coordinates":[1.0,2.0]}

SELECT ST_AsGeoJSON('POLYGON((101 345 1, 300 345 2, 300 100 2, 
                              101 100 2, 101 345 1), 
                             (130 300 2, 190 300 2, 190 220 2, 
                              130 220 2, 130 300 2))');
-- Answer: {"type":"Polygon","coordinates":[[[101.0,345.0,1.0],
--     [300.0,345.0,2.0], [300.0,100.0,2.0], [101.0,100.0,2.0],
--     [101.0,345.0,1.0]], 
--    [[130.0,300.0,2.0], [190.0,300.0,2.0],[190.0,220.0,2.0], 
--     [130.0,220.0,2.0], [130.0,300.0,2.0]]]}
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/geojson/ST_AsGeoJSON.java" target="_blank">Source code</a>

