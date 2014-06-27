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
GPXRead(VARCHAR fileName);
GPXRead(VARCHAR fileName, VARCHAR tableReference);
{% endhighlight %}

### Description
Reads a GPX file and copy the content in the specified tables.

### Examples

{% highlight mysql %}
CALL GPXRead('route.gpx', 'DATABASE.PUBLIC.GPXDATA');
SELECT a.the_geom line, b.the_geom point, c.the_geom segment
FROM GPXDATA_track a, GPXDATA_trackPoint b, GPXDATA_trackSegment c;
-- Answer:
-- |         line         |       point        |       segment       |
-- | -------------------- | ------------------ | ------------------- |
-- | MULTILINESTRING(     | POINT(-1.55 47.16) | LINESTRING(         |
-- | (-1.55 47.16,        | POINT(-1.60 47.10) | -1.55 47.16,        |
-- | -1.60 47.10, -1 47)) | POINT(-1 47)       | -1.60 47.10, -1 47) |

SELECT * FROM GPXDATA_track;
-- Answer:
-- |                       THE_GEOM                      |  ID |         NAME         | CMT  | DESC | SRC  | HREF | HREF_TITLE | NUMBER | TYPE | EXTENSIONS |
-- | --------------------------------------------------- | --- | -------------------- | ---- | ---- | ---- | ---- | ---------- | ------ | ---- | ---------- |
-- | MULTILINESTRING((-1.55 47.16, -1.60 47.10, -1 47))  |   1 | 2014-04-23T10:55:03Z | null | null | null | null | null       | null   | null | null       |

SELECT * FROM GPXDATA_trackPoint;
-- Answer:
-- |       THE_GEOM      |  ID |  LAT  |  LON  | ELE | TIME | MAGVAR | GEOIDHEIGHT | NAME | CMT  | DESC | SRC  | HREF | HREF_TITLE | SYM  | TYPE | FIX  | SAT  | HDOP | VDOP | PDOP | AGEOFDGPSDATA | DGPSID | EXTENSIONS | TRACK_SEGMENT_ID |
-- | ------------------- | --- | ----- | ----- | --- | ---- | ------ | ----------- | ---- | ---- | ---- | ---- | ---- | ---------- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ------------- | ------ | ---------- | ---------------- |
-- | POINT(-1.55 47.16)  |   2 | 47.16 | -1.55 | NaN | null | null   | null        | null | null | null | null | null | null       | null | null | null | null | null | null | null | null          | null   | null       |                2 |

CALL GPXRead('station.gpx');
SELECT * FROM station_WAYPOINT;
-- Answer: POINT(-71.119277 42.438878)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/gpx/GPXRead.java" target="_blank">Source code</a>
