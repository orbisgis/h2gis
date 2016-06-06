---
layout: docs
title: GeoJsonWrite
category: h2drivers
is_function: true
description: Table &rarr; GeoJSON
prev_section: GeoJsonRead
next_section: KMLWrite
permalink: /docs/1.2/GeoJsonWrite/
---

### Signature

{% highlight mysql %}
GeoJsonWrite(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Writes table `tableName` to a [GeoJSON][wiki] file located at
`path`.

### Examples

{% highlight mysql %}
-- Write a spatial table to a GeoJSON file:
CREATE TABLE TEST(ID INT PRIMARY KEY, THE_GEOM POINT);
INSERT INTO TEST VALUES (1, 'POINT(0 1)');
CALL GeoJsonWrite('/home/user/test.geojson', 'TEST');

-- Read it back:
CALL GeoJsonRead('/home/user/test.geojson', 'TEST2');
SELECT * FROM TEST2;
-- Answer:
-- | THE_GEOM    | ID |
-- |-------------|----|
-- | POINT(0 1)  | 1  |
{% endhighlight %}

##### See also

* [`GeoJsonRead`](../GeoJsonRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2drivers/src/main/java/org/h2gis/drivers/geojson/GeoJsonWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
