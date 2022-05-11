---
layout: docs
title: GeoJsonWrite
category: h2drivers
is_function: true
description: Table &rarr; GeoJSON
prev_section: GeoJsonRead
next_section: JsonWrite
permalink: /docs/dev/GeoJsonWrite/
---

### Signature

{% highlight mysql %}
GeoJsonWrite(VARCHAR path, VARCHAR tableName);
GeoJsonWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
{% endhighlight %}

### Description

Writes the contents of table `tableName` to a [GeoJSON][wiki] file located at
`path`.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`).

The default value of `fileEncoding` is `ISO-8859-1`.

### Examples

{% highlight mysql %}
-- Write a spatial table to a GeoJSON file:
CREATE TABLE TEST(ID INT PRIMARY KEY, GEOM POINT);
INSERT INTO TEST VALUES (1, 'POINT(0 1)');
INSERT INTO TEST VALUES (2, 'POINT(2 4)');

CALL GeoJsonWrite('/home/user/test.geojson', 'TEST');

-- Read it back:
CALL GeoJsonRead('/home/user/test.geojson', 'TEST2');
SELECT * FROM TEST2;
-- Answer:
-- |     GEOM    | ID |
-- |-------------|----|
-- | POINT(0 1)  | 1  |
-- | POINT(2 4)  | 2  |
{% endhighlight %}

#### Case where `tablename` is the result of a selection

{% highlight mysql %}
CALL GeoJsonWrite('/home/user/test.geojson', 
                  '(SELECT * FROM TEST WHERE ID<2 )');

-- Read it back:
CALL GeoJsonRead('/home/user/test.geojson', 'TEST2');
SELECT * FROM TEST2;
-- Answer:
-- |     GEOM    | ID |
-- |-------------|----|
-- | POINT(0 1)  | 1  |
{% endhighlight %}

##### See also

* [`GeoJsonRead`](../GeoJsonRead), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
