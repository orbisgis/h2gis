# GeoJsonRead

## Signatures

```sql
GeoJsonRead(VARCHAR path);
GeoJsonRead(VARCHAR path, VARCHAR tableName);
```

## Description

Reads a [GeoJSON][wiki] file from `path` and creates the
corresponding spatial table `tableName`.

If the `tablename` parameter is not specified, then the resulting table has the same name as the GeoJSON file.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

## Examples

```sql
CALL GeoJsonRead('/home/user/data.geojson');
```

&rarr; Here `data.geojson` will produce a table named `data`.

```sql
CALL GeoJsonRead('/home/user/data.geojson', 'NEW_DATA');
```

&rarr; Here `data.geojson` will produce a table named `NEW_DATA`.

## See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
