# GeoJsonWrite

## Signatures

```sql
GeoJsonWrite(VARCHAR path, VARCHAR tableName);
GeoJsonWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
```

## Description

Writes the contents of table `tableName` to a [GeoJSON][wiki] file located at `path`.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.

The `.geojson` file may be zipped in a `.gz` file *(in this case, the `GeoJsonWrite` driver will zip on the fly the `.geojson` file)*. 

If the `deleteTable` parameter is `true` and `path` file already exists, then `path` file will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `path` file already exists will be throwned.

## Examples

In the following example, we are working with a table named `TEST` and defined as follow.

```sql
CREATE TABLE TEST(ID INT PRIMARY KEY, THE_GEOM GEOMETRY(POINT));
INSERT INTO TEST VALUES (1, 'POINT(0 1)');
INSERT INTO TEST VALUES (2, 'POINT(2 4)');
```

### 1. Case with `path` and `tableName`

```sql
-- Write a spatial table to a GeoJSON file:
CALL GeoJsonWrite('/home/user/test.geojson', 'TEST');

-- Read it back:
CALL GeoJsonRead('/home/user/test.geojson', 'TEST2');
SELECT * FROM TEST2;
```

Answer:
| ID | THE_GEOM    |
|----|-------------|
| 1  | POINT(0 1)  |
| 2  | POINT(2 4)  |

If you want to compress your resulting `.geojson` file into a `.gz` file, just execute

```sql
CALL GeoJsonWrite('/home/user/test.geojson.gz', 'TEST');
```
As a result, you will obtain a `test.geojson.gz` file in which there is the `test.geojson` resulting file.

### 2. Case where `tableName` is the result of a selection

```sql
CALL GeoJsonWrite('/home/user/test.geojson', 
                  '(SELECT * FROM TEST WHERE ID<2 )');
-- Read it back:
CALL GeoJsonRead('/home/user/test.geojson', 'TEST2');
SELECT * FROM TEST2;
```

Answer:

| ID | THE_GEOM    |
|----|-------------|
| 1  | POINT(0 1)  |


## See also

* [`GeoJsonRead`](../GeoJsonRead), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
