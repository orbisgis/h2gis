---
layout: docs
title: GPXRead
category: h2drivers
is_function: true
description: Read a GPX file
prev_section: FILE_TABLE
next_section: GeoJsonRead
permalink: /docs/dev/GPXRead/
---

### Signatures

{% highlight mysql %}
GPXRead(VARCHAR path);
GPXRead(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Reads a [GPX][wiki] file from `path` and creates several tables
prefixed by `tableName` representing the file's contents.
By default, the `tableName` is taken from the filename
given in `path`.

### Examples

{% highlight mysql %}
-- Produces GPXDATA_TRACK, GPXDATA_TRACKPOINT, GPXDATA_TRACKSEGMENT
CALL GPXRead('/home/user/route.gpx', 'GPXDATA');

-- Produces ROUTE_TRACK, ROUTE_TRACKPOINT, ROUTE_TRACKSEGMENT
CALL GPXRead('/home/user/route.gpx');
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2drivers/src/main/java/org/h2gis/drivers/gpx/GPXRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GPS_eXchange_Format
