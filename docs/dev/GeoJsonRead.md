---
layout: docs
title: GeoJsonRead
category: h2drivers
is_function: true
description: GeoJSON &rarr; Table
prev_section: GPXRead
next_section: GeoJsonWrite
permalink: /docs/dev/GeoJsonRead/
---

### Signature

{% highlight mysql %}
GeoJsonRead(VARCHAR path);
GeoJsonRead(VARCHAR path, BOOLEAN deleteTable);

GeoJsonRead(VARCHAR path, VARCHAR tableName);
GeoJsonRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);

GeoJsonRead(VARCHAR path, VARCHAR tableName, 
            VARCHAR fileEncoding);
GeoJsonRead(VARCHAR path, VARCHAR tableName, 
            VARCHAR fileEncoding, BOOLEAN deleteTable);
{% endhighlight %}

### Description

Reads a [GeoJSON][wiki] file from `path` and creates the corresponding spatial table `tableName`. This `.geojson` file may be zipped in a `.gz` file *(in this case, the `GeoJsonRead` driver will unzip on the fly the `.gz` file)*.

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If:

- the `tablename` parameter is not specified, then the resulting table has the same name as the GeoJSON file.
- the `deleteTable` parameter is `true` and table `tableName` already exists in the database, then table `tableName` will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the table `tableName` already exists will be throwned.


<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

### Examples

##### 1. Case with `path`

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson');
{% endhighlight %}

&rarr; Here `data.geojson` will produce a table named `data`.

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson.gz');
{% endhighlight %}

&rarr; Here `data.geojson.gz` will produce a table named `data_geojson`.

##### 2. Case with `tableName`

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson', 'NEW_DATA');
{% endhighlight %}

&rarr; Here `data.geojson` will produce a table named `NEW_DATA`.

##### 3. Case with `fileEncoding`

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson', 'NEW_DATA', 'utf-8');
{% endhighlight %}

##### 4. Case with `deleteTable`

Load the `data.geojson` file
{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson');
{% endhighlight %}

&rarr; the table `data` is created.

Now, load once again, using `deleteTable` = `true`

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson', true);
{% endhighlight %}

&rarr; the already existing `data` table is removed / replaced.

Now, load once again, using `deleteTable` = `false`

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson', false);
{% endhighlight %}

&rarr; Error message: `The table "DATA" already exists`.

##### See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
