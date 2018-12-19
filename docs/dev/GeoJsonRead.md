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
GeoJsonRead(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Reads a [GeoJSON][wiki] file from `path` and creates the
corresponding spatial table `tableName`.

If the `tablename` parameter is not specified, then the resulting table has the same name as the GeoJSON file.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

### Examples

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson');
{% endhighlight %}

&rarr; Here `data.geojson` will produce a table named `data`.

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson', 'NEW_DATA');
{% endhighlight %}

&rarr; Here `data.geojson` will produce a table named `NEW_DATA`.

##### See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
