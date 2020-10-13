---
layout: docs
title: JsonWrite
category: h2drivers
is_function: true
description: Table &rarr; JSON
prev_section: jsonWrite
next_section: KMLWrite
permalink: /docs/dev/JsonWrite/
---

### Signature

{% highlight mysql %}
JsonWrite(VARCHAR path, VARCHAR tableName);
JsonWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
JsonWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
JsonWrite(VARCHAR path, VARCHAR tableName, 
          VARCHAR fileEncoding, BOOLEAN deleteTable);
{% endhighlight %}

### Description

Writes table `tableName` to a [JSON][wiki] file located at `path`.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.

The `.json` file may be zipped in a `.gz` file *(in this case, the `JsonWrite` driver will zip on the fly the `.json` file)*. 

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If the `deleteTable` parameter is `true` and `path` file already exists, then `path` file will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `path` file already exists will be throwned.

### Examples

In the following example, we are working with a table named `TEST` and defined as follow.
{% highlight mysql %}
CREATE TABLE TEST(ID INT, THE_GEOM GEOMETRY(POINT), NAME VARCHAR);
INSERT INTO TEST VALUES (1, 'POINT(0 1)', 'Paris'),
                        (2, 'POINT(2 4)', 'Orléans');
{% endhighlight %}

#### 1. Case with `path` and `tableName`

Export the spatial table into a JSON file:
{% highlight mysql %}
CALL JsonWrite('/home/user/test.json', 'TEST');
{% endhighlight %}

Open the `test.json` file.
{% highlight json %}
{"ID":1,"THE_GEOM":"POINT (0 1)","NAME":"Paris"} 
{"ID":2,"THE_GEOM":"POINT (2 4)","NAME":"Orléans"}
{% endhighlight json %}

If you want to compress your resulting `.json` file into a `.gz` file, just execute

{% highlight mysql %}
CALL JsonWrite('/home/user/test.json.gz', 'TEST');
{% endhighlight mysql %}

As a result, you will obtain a `test.json.gz` file in which there is the `test.json` resulting file.

#### 2. Case where `tableName` is the result of a selection

{% highlight mysql %}
CALL JsonWrite('/home/user/test.json', 
       '(SELECT * FROM TEST WHERE NAME=''Orléans'' )');
{% endhighlight %}

Open the `test.json` file.
{% highlight json %}
{"ID":2,"THE_GEOM":"POINT (2 4)","NAME":"Orléans"}
{% endhighlight json %}


#### 3. Case with `fileEncoding`

{% highlight mysql %}
CALL JsonWrite('/home/user/test.json', 'TEST', 'utf-8');
{% endhighlight %}

#### 4. Case with `deleteTable`
We condisder that the `test.json` already exists here `/home/user/`
{% highlight mysql %}
CALL JsonWrite('/home/gpetit/test.json', 'TEST', true);
-- or
CALL JsonWrite('/home/user/test.json', 'TEST', 'utf-8', true);
{% endhighlight %}
Since we have `deleteTable` = `true`, the file `test.json` is overwritten.

Now, execute with `deleteTable` = `false`
{% highlight mysql %}
CALL JsonWrite('/home/gpetit/test.json', 'TEST', false);
-- or
CALL JsonWrite('/home/user/test.json', 'TEST', 'utf-8', false);
{% endhighlight %}

An error message is throwned: `The json file already exists`



##### See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`GeoJsonRead`](../GeoJsonRead), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/json/JsonWrite.java" target="_blank">Source code</a>

[wiki]: https://fr.wikipedia.org/wiki/JavaScript_Object_Notation
