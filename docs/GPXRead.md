# GPXRead

## Signatures

```sql
GPXRead(VARCHAR path);
GPXRead(VARCHAR path, VARCHAR tableName);
GPXRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTables);
```

## Description

Reads a [GPX][wiki] file from `path` and creates several tables
prefixed by `tableName` representing the file's contents. If `deleteTables` is equal to `true`, existing tables (with the same prefix) are removed.


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

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

## Examples

```sql
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
CALL GPXRead('/home/user/route.gpx', 'GPXDATA', true);

-- Produces STATION_WAYPOINT.
CALL GPXRead('/home/user/station.gpx');
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/gpx/GPXRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GPS_eXchange_Format
