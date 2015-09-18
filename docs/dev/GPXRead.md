---
layout: docs
title: GPXRead
category: h2drivers
is_function: true
description: GPX &rarr; Table
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
Which tables are produced depends on the content of the GPX file,
and may include:

* `TABLENAME_WAYPOINT`
* `TABLENAME_ROUTE`
* `TABLENAME_ROUTEPOINT`
* `TABLENAME_TRACK`
* `TABLENAME_TRACKPOINT`
* `TABLENAME_TRACKSEGMENT`

By default, the `tableName` is the filename given in `path` without
the extension.

### Examples

{% highlight mysql %}
-- Takes the table name from the filename, producing
-- * ROUTE_TRACK
-- * ROUTE_TRACKPOINT
-- * ROUTE_TRACKSEGMENT
CALL GPXRead('/home/user/route.gpx');

-- Uses the given table name, producing
-- * GPXDATA_TRACK
-- * GPXDATA_TRACKPOINT
-- * GPXDATA_TRACKSEGMENT
CALL GPXRead('/home/user/route.gpx', 'GPXDATA');

-- Produces STATION_WAYPOINT.
CALL GPXRead('/home/user/station.gpx');
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2drivers/src/main/java/org/h2gis/drivers/gpx/GPXRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GPS_eXchange_Format
