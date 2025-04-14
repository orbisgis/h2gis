# JsonWrite

## Signature

```sql
JsonWrite(VARCHAR path, VARCHAR tableName);
```

## Description

Writes table `tableName` to a [JSON][wiki] file located at `path`.

## Examples

Write a spatial table and export it into a JSON file.

```sql
-- Initialize the spatial table
CREATE TABLE TEST(ID INT PRIMARY KEY, THE_GEOM POINT);
INSERT INTO TEST VALUES (1, 'POINT(0 1)');
-- Export
CALL JsonWrite('/home/user/test.json', 'TEST');
```

Open the `test.json` file.

```json
{"ID":1,"THE_GEOM":"POINT (0 1)"}
```

## See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`GeoJsonRead`](../GeoJsonRead), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/json/JsonWrite.java" target="_blank">Source code</a>

[wiki]: https://fr.wikipedia.org/wiki/JavaScript_Object_Notation
