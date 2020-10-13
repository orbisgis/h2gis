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
GeoJsonWrite(VARCHAR path, VARCHAR tableName, 
             BOOLEAN deleteTable);
GeoJsonWrite(VARCHAR path, VARCHAR tableName, 
             VARCHAR fileEncoding);
GeoJsonWrite(VARCHAR path, VARCHAR tableName, 
             VARCHAR fileEncoding, BOOLEAN deleteTable);
{% endhighlight %}

### Description

Writes the contents of table `tableName` to a [GeoJSON][wiki] file located at `path`.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.


The `.geojson` file may be zipped in a `.gz` file *(in this case, the `GeoJsonWrite` driver will zip on the fly the `.geojson` file)*. 

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If the `deleteTable` parameter is `true` and `path` file already exists, then `path` file will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `path` file already exists will be throwned.


### Examples

In the following example, we are working with a table named `TEST` and defined as follow.
{% highlight mysql %}
CREATE TABLE TEST(ID INT PRIMARY KEY, THE_GEOM GEOMETRY(POINT));
INSERT INTO TEST VALUES (1, 'POINT(0 1)');
INSERT INTO TEST VALUES (2, 'POINT(2 4)');
{% endhighlight %}

#### 1. Case with `path` and `tableName`

{% highlight mysql %}
-- Write a spatial table to a GeoJSON file:
CALL GeoJsonWrite('/home/user/test.geojson', 'TEST');

-- Read it back:
CALL GeoJsonRead('/home/user/test.geojson', 'TEST2');
SELECT * FROM TEST2;
-- Answer:
-- | ID | THE_GEOM    |
-- |----|-------------|
-- | 1  | POINT(0 1)  |
-- | 2  | POINT(2 4)  |
{% endhighlight %}

If you want to compress your resulting `.geojson` file into a `.gz` file, just execute

{% highlight mysql %}
CALL GeoJsonWrite('/home/user/test.geojson.gz', 'TEST');
{% endhighlight mysql %}

As a result, you will obtain a `test.geojson.gz` file in which there is the `test.geojson` resulting file.

#### 2. Case where `tableName` is the result of a selection

{% highlight mysql %}
CALL GeoJsonWrite('/home/user/test.geojson', 
                  '(SELECT * FROM TEST WHERE ID<2 )');

-- Read it back:
CALL GeoJsonRead('/home/user/test.geojson', 'TEST2');
SELECT * FROM TEST2;
-- Answer:
-- | ID | THE_GEOM    |
-- |----|-------------|
-- | 1  | POINT(0 1)  |
{% endhighlight %}

#### 3. Case with `fileEncoding`

{% highlight mysql %}
CALL GeoJsonWrite('/home/user/test.geojson', 'TEST', 'utf-8');
{% endhighlight %}

#### 4. Case with `deleteTable`
We condisder that the `test.geojson` already exists here `/home/user/`
{% highlight mysql %}
CALL GeoJsonWrite('/home/gpetit/test.geojson', 'TEST', true);
-- or
CALL GeoJsonWrite('/home/user/test.geojson', 'TEST', 'utf-8', true);
{% endhighlight %}
Since we have `deleteTable` = `true`, the file `test.geojson` is overwritten.

Now, execute with `deleteTable` = `false`
{% highlight mysql %}
CALL GeoJsonWrite('/home/gpetit/test.geojson', 'TEST', false);
-- or
CALL GeoJsonWrite('/home/user/test.geojson', 'TEST', 'utf-8', false);
{% endhighlight %}

An error message is throwned: `The geojson file already exists`

##### See also

* [`GeoJsonRead`](../GeoJsonRead), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
