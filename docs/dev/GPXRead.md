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
GPXRead(VARCHAR path, BOOLEAN deleteTable);
GPXRead(VARCHAR path, VARCHAR tableName);
GPXRead(VARCHAR path, VARCHAR tableName, 
        BOOLEAN deleteTable);
GPXRead(VARCHAR path, VARCHAR tableName, 
        VARCHAR fileEncoding);
GPXRead(VARCHAR path, VARCHAR tableName, 
        VARCHAR fileEncoding, BOOLEAN deleteTable);


{% endhighlight %}

### Description

Reads a [GPX][wiki] file from `path` and creates several tables prefixed by `tableName` representing the file's contents. 

Tables are produced depending on the content of the GPX file, and may include:

* `TABLENAME_WAYPOINT`
* `TABLENAME_ROUTE`
* `TABLENAME_ROUTEPOINT`
* `TABLENAME_TRACK`
* `TABLENAME_TRACKPOINT`
* `TABLENAME_TRACKSEGMENT`

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If:

- the `tablename` parameter is not specified, then the resulting tables are prefixed with the same name as the GPX file.
- the `deleteTable` parameter is `true` and tables prefixed with `tableName` already exists in the database, then tables `tableName` will be removed / replaced by the new ones. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the tables prefixed with `tableName` already exists will be throwned.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

### Examples

In the following example, we are using two `.gpx` files presented below *(and coming from this [webpage](https://www.rigacci.org/wiki/doku.php/tecnica/gps_cartografia_gis/gpx))* and stored in `/home/user/`:

Track file : `road.gpx`

{% highlight xml %}
<trk>
 <name>ACTIVE LOG</name>
 <trkseg>
  <trkpt lat="43.858259" lon="11.097178">
    <ele>66.468262</ele>
    <time>2005-03-20T07:20:37Z</time>
  </trkpt>
  <trkpt lat="43.858280" lon="11.097243">
    <ele>40.032104</ele>
    <time>2005-03-20T07:20:49Z</time>
  </trkpt>
  <trkpt lat="43.858280" lon="11.097114">
    <ele>40.512817</ele>
    <time>2005-03-20T07:20:57Z</time>
  </trkpt>
 </trkseg>
</trk>
{% endhighlight %}

Waypoints file : `station.gpx`

{% highlight xml %}
<wpt lat="43.148408839" lon="10.853555845">
  <ele>74.387085</ele>
  <name>020</name>
  <cmt>020</cmt>
  <desc>020</desc>
  <sym>Flag</sym>
</wpt>
{% endhighlight %}

#### 1. Using `path`

**a. With a `track` file**

{% highlight mysql %}
CALL GPXRead('/home/user/road.gpx');
{% endhighlight %}

Returns the following tables:

- ROAD_TRACK
- ROAD_TRACKPOINT
- ROAD_TRACKSEGMENT

**b. With a `waypoints` file**

{% highlight mysql %}
CALL GPXRead('/home/user/station.gpx');
{% endhighlight %}

Returns the following table:

- STATION_WAYPOINT

#### 2. Using `path` and `tableName`

{% highlight mysql %}
CALL GPXRead('/home/user/road.gpx', 'GPXROAD');
{% endhighlight %}

Returns the following tables:

- GPXROAD_TRACK
- GPXROAD_TRACKPOINT
- GPXROAD_TRACKSEGMENT

##### 3. Case with `fileEncoding`

{% highlight mysql %}
CALL GPXRead('/home/user/road.gpx', 'GPXROAD', 'utf-8');
{% endhighlight %}

##### 4. Case with `deleteTable`

Load the `road.gpx` file
{% highlight mysql %}
CALL GPXRead('/home/user/road.gpx', 'GPXROAD');
{% endhighlight %}

&rarr; the tables `GPXROAD_TRACK`, `GPXROAD_TRACKPOINT` and `GPXROAD_TRACKSEGMENT` are created.

Now, load once again, using `deleteTable` = `true`

{% highlight mysql %}
CALL GPXRead('/home/user/road.gpx', 'GPXROAD', true);
{% endhighlight %}

&rarr; the already existing `GPXROAD_` tables are removed / replaced.

Now, load once again, using `deleteTable` = `false`

{% highlight mysql %}
CALL GPXRead('/home/user/road.gpx', 'GPXROAD', false);
{% endhighlight %}

&rarr; Error message: `The table "GPXROAD_TRACK" already exists`.

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/gpx/GPXRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GPS_eXchange_Format
