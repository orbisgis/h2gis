# GeoJsonTable

## Signatures

```sql
GeoJsonTable(VARCHAR path);
GeoJsonTable(VARCHAR path, VARCHAR columns);
```

## Description

Reads a [GeoJSON][wiki] file from `path` as a table, without importing it, like `CSVREAD` for a CSV file. The file may be zipped in a `.gz` file.

`GeoJsonTable` is used in the `FROM` clause of a query: the features can be filtered, joined or aggregated directly, or only the useful columns and rows imported with `CREATE TABLE ... AS SELECT`.

The columns and their types are the same as with [`GeoJsonRead`](../GeoJsonRead): the geometry in the `THE_GEOM` column, then one column per property found in the features.

`columns` lists the columns to read, separated by commas (`THE_GEOM` for the geometry), in the order of the result. The names are case insensitive, unless they are quoted. Reading only some columns is faster: the geometry and the properties that are not listed are skipped. When `columns` is `NULL` or empty, all the columns are read.

The file must be encoded in UTF-8, as required by the GeoJSON specification ([RFC 7946](https://www.rfc-editor.org/rfc/rfc7946)).

:::{note}
**The file is read at each query**

## Examples

```sql
SELECT * FROM GeoJsonTable('/home/user/building.geojson');
```

```sql
SELECT COUNT(*), AVG(HEIGHT_WALL) FROM GeoJsonTable('/home/user/building.geojson.gz') WHERE TYPE = 'house';
```

Read only the needed columns, faster:

```sql
SELECT AVG(HEIGHT_WALL) FROM GeoJsonTable('/home/user/building.geojson', 'HEIGHT_WALL');
```

Import only some columns and rows:

```sql
CREATE TABLE HIGH_BUILDING AS
    SELECT * FROM GeoJsonTable('/home/user/building.geojson', 'THE_GEOM, HEIGHT_WALL')
    WHERE HEIGHT_WALL > 10;
```

## See also

* [`GeoJsonRead`](../GeoJsonRead), [`GeoJsonWrite`](../GeoJsonWrite), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonTable.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
