---
layout: docs
title: ST_AsGeoJson
category: h2drivers
is_function: true
description: Convert a Geometry to its GeoJSON representation
prev_section: SHPWrite
next_section: ST_AsKML
permalink: /docs/dev/ST_AsGeoJson/
---

### Signature

{% highlight mysql %}
VARCHAR ST_AsGeoJson(GEOMETRY geom);
{% endhighlight %}

### Description

Converts `geom` to its [GeoJSON][wiki] representation.
Both 2D and 3D Geometries are supported.

### Examples

{% highlight mysql %}
SELECT ST_AsGeoJSON('POINT(1 2)');
-- Answer: {"type":"Point","coordinates":[1.0,2.0]}

SELECT ST_AsGeoJSON('POLYGON((101 345 1, 300 345 2, 300 100 2,
                              101 100 2, 101 345 1),
                             (130 300 2, 190 300 2, 190 220 2,
                              130 220 2, 130 300 2))');
-- Answer: {"type":"Polygon", "coordinates":[[[101.0, 345.0, 1.0],
--     [300.0, 345.0, 2.0], [300.0, 100.0, 2.0], [101.0, 100.0, 2.0],
--     [101.0, 345.0, 1.0]],
--    [[130.0, 300.0, 2.0], [190.0, 300.0, 2.0], [190.0, 220.0, 2.0],
--     [130.0, 220.0, 2.0], [130.0, 300.0, 2.0]]]}
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/geojson/ST_AsGeoJSON.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
