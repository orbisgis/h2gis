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
GPXRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTables);
{% endhighlight %}

### Description

Reads a [GPX][wiki] file from `path` and creates several tables
prefixed by `tableName` representing the file's contents. If `deleteTables` is equal to `1`, existing tables (with the same prefix) are removed.


Tables are produced depending on the content of the GPX file,
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

-- Existing tables starting with 'GPXDATA' will be removed
CALL GPXRead('/home/user/route.gpx', 'GPXDATA', 1);

-- Produces STATION_WAYPOINT.
CALL GPXRead('/home/user/station.gpx');
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/gpx/GPXRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GPS_eXchange_Format
