---
layout: docs
title: KMLWrite
category: h2drivers
is_function: true
description: Table &rarr; KML, KMZ 
prev_section: JsonWrite
next_section: SHPRead
permalink: /docs/dev/KMLWrite/
---

### Signature

{% highlight mysql %}
KMLWrite(VARCHAR path, VARCHAR tableName);
KMLWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
KMLWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
KMLWrite(VARCHAR path, VARCHAR tableName, 
         VARCHAR fileEncoding, BOOLEAN deleteTable);
{% endhighlight %}

### Description

Writes table `tableName` to a [KML][wiki] file located at `path`.
A coordinate reference system must be set to save a KML file.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.

The `.kml` file may be zipped in a `.kmz` file *(in this case, the `KMLWrite` driver will zip on the fly the `.kml` file)*. 

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If the `deleteTable` parameter is `true` and `path` file already exists, then `path` file will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `path` file already exists will be throwned.



### Examples

In the following example, we are working with a table named `TEST` and defined as follow.

{% highlight mysql %}
CREATE TABLE TEST(ID INT, THE_GEOM GEOMETRY (POINT));
INSERT INTO TEST VALUES 
	(1, ST_GeomFromText('POINT(2.19 47.58)', 4326)),
	(2, ST_GeomFromText('POINT(2.45 48.17)', 4326)),
	(3, ST_GeomFromText('POINT(2.95 48.63)', 4326));
{% endhighlight %}

#### 1. Case with `path` and `tableName`

{% highlight mysql %}
-- Write it as a KML
CALL KMLWrite('/home/user/test.kml', 'TEST');
-- or as a KMZ
CALL KMLWrite('/home/user/test.kmz', 'TEST');
{% endhighlight %}

#### 2. Case where `tableName` is the result of a selection

{% highlight mysql %}
CALL KMLWrite('/home/user/test.kml', 
                  '(SELECT * FROM TEST WHERE ID<2 )');
{% endhighlight %}

#### 3. Case with `fileEncoding`

{% highlight mysql %}
CALL KMLWrite('/home/user/test.kml', 'TEST', 'utf-8');
{% endhighlight %}

#### 4. Case with `deleteTable`
We condisder that the `test.kml` already exists here `/home/user/`
{% highlight mysql %}
CALL KMLWrite('/home/gpetit/test.kml', 'TEST', true);
-- or
CALL KMLWrite('/home/user/test.kml', 'TEST', 'utf-8', true);
{% endhighlight %}
Since we have `deleteTable` = `true`, the file `test.kml` is overwritten.

Now, execute with `deleteTable` = `false`
{% highlight mysql %}
CALL KMLWrite('/home/gpetit/test.kml', 'TEST', false);
-- or
CALL KMLWrite('/home/user/test.kml', 'TEST', 'utf-8', false);
{% endhighlight %}

An error message is throwned: `The kml file already exists`


##### See also

* [`ST_AsKML`](../ST_AsKML)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/kml/KMLWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Keyhole_Markup_Language
