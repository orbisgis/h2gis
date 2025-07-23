# JsonWrite

## Signature

```sql
JsonWrite(VARCHAR path, VARCHAR tableName);
JsonWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
```

## Description

Writes table `tableName` to a [JSON][wiki] file located at `path`.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.

The `.json` file may be zipped in a `.gz` file *(in this case, the `JsonWrite` driver will zip on the fly the `.json` file)*. 


If the `deleteTable` parameter is `true` and `path` file already exists, then `path` file will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `path` file already exists will be throwned.

## Examples

In the following example, we are working with a table named `TEST` and defined as follow.
```sql
CREATE TABLE TEST(ID INT, THE_GEOM GEOMETRY(POINT), NAME VARCHAR);
INSERT INTO TEST VALUES (1, 'POINT(0 1)', 'Paris'),
                        (2, 'POINT(2 4)', 'Orléans');
```

### 1. Case with `path` and `tableName`

Export the spatial table into a JSON file:

```sql
CALL JsonWrite('/home/user/test.json', 'TEST');
```

Open the `test.json` file.

```json
{"ID":1,"THE_GEOM":"POINT (0 1)","NAME":"Paris"} 
{"ID":2,"THE_GEOM":"POINT (2 4)","NAME":"Orléans"}
```

If you want to compress your resulting `.json` file into a `.gz` file, just execute

```sql
CALL JsonWrite('/home/user/test.json.gz', 'TEST');
```

As a result, you will obtain a `test.json.gz` file in which there is the `test.json` resulting file.

### 2. Case where `tableName` is the result of a selection

```sql
CALL JsonWrite('/home/user/test.json', 
       '(SELECT * FROM TEST WHERE NAME=''Orléans'' )');
```

Open the `test.json` file.
```json
{"ID":2,"THE_GEOM":"POINT (2 4)","NAME":"Orléans"}
```

### 3. Case with `deleteTable`

We condisder that the `test.json` already exists here `/home/user/`
```sql
CALL JsonWrite('/home/gpetit/test.json', 'TEST', true);
```

Since we have `deleteTable` = `true`, the file `test.json` is overwritten.

Now, execute with `deleteTable` = `false`

```sql
CALL JsonWrite('/home/gpetit/test.json', 'TEST', false);
```

An error message is throwned: `The json file already exists`

## See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`GeoJsonRead`](../GeoJsonRead), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/json/JsonWrite.java" target="_blank">Source code</a>

[wiki]: https://fr.wikipedia.org/wiki/JavaScript_Object_Notation
