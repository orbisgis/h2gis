---
layout: docs
title: ST_GeomFromGeoJson
category: h2drivers
is_function: true
description: GeoJSON &rarr; Geometry
prev_section: ST_AsKML
next_section: ST_OSMDownloader
permalink: /docs/1.5.0/ST_GeomFromGeoJson/
---

### Signature

{% highlight mysql %}
VARCHAR ST_GeomFromGeoJson(VARCHAR geojson);
{% endhighlight %}

### Description

Converts a `geojson` (more details [here][wiki]) representation of a geometry into a `geometry` object.

Both 2D and 3D Geometries are supported.

### Examples

{% highlight mysql %}
SELECT ST_GeomFromGeoJson('
	{"type":"Point",
	 "coordinates":[1.0,2.0]}');
-- Answer: POINT (1 2)

SELECT ST_GeomFromGeoJson('
	{"type":"LineString",
	 "coordinates":[[1,2], [3,4], [5,6]]}');
-- Answer: LINESTRING (1 2, 3 4, 5 6)

SELECT ST_GeomFromGeoJson('
	{"type":"Polygon",
	 "coordinates":[[[1,2], [3,4], [5,6], [7,8], [1,2]]]}');
-- Answer: POLYGON ((1 2, 3 4, 5 6, 7 8, 1 2)) 
{% endhighlight %}

##### See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`GeoJsonRead`](../GeoJsonRead), [`ST_AsGeoJson`](../ST_AsGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/ST_GeomFromGeoJSON.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
