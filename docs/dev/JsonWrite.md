---
layout: docs
title: JsonWrite
category: h2drivers
is_function: true
description: Table &rarr; JSON
prev_section: GeoJsonWrite
next_section: KMLWrite
permalink: /docs/dev/JsonWrite/
---

### Signature

{% highlight mysql %}
JsonWrite(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Writes table `tableName` to a [JSON][wiki] file located at `path`.

### Examples

Write a spatial table and export it into a JSON file.

{% highlight mysql %}
-- Initialize the spatial table
CREATE TABLE TEST(ID INT PRIMARY KEY, GEOM POINT);
INSERT INTO TEST VALUES (1, 'POINT(0 1)');
-- Export
CALL JsonWrite('/home/user/test.json', 'TEST');
{% endhighlight mysql %}

Open the `test.json` file.

{% highlight json %}
{"ID":1,"GEOM":"POINT (0 1)"}
{% endhighlight json %}

##### See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`GeoJsonRead`](../GeoJsonRead), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/json/JsonWrite.java" target="_blank">Source code</a>

[wiki]: https://fr.wikipedia.org/wiki/JavaScript_Object_Notation
