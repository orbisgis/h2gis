# GeoJsonRead

## Signatures

```sql
GeoJsonRead(VARCHAR path);
GeoJsonRead(VARCHAR path, BOOLEAN deleteTable);

GeoJsonRead(VARCHAR path, VARCHAR tableName);
GeoJsonRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);

GeoJsonRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
GeoJsonRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding, BOOLEAN deleteTable);
```

## Description

Reads a [GeoJSON][wiki] file from `path` and creates the corresponding spatial table `tableName`. This `.geojson` file may be zipped in a `.gz` file *(in this case, the `GeoJsonRead` driver will unzip on the fly the `.gz` file)*.

:::{warning}
The input geometries coordinates have to be exprimed with one of these 3 options:
* `X` and `Y`,
* `X`, `Y` and `Z`,
* `X`, `Y`, `Z` and `M`.
:::

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If:

- the `tablename` parameter is not specified, then the resulting table has the same name as the GeoJSON file.
- the `deleteTable` parameter is `true` and table `tableName` already exists in the database, then table `tableName` will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the table `tableName` already exists will be throwned.

:::{note}
**Warning on the input file name**

When a `tablename` is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: `A to Z`, `_` and `0 to 9`.
:::

## Examples

### 1. Case with `path`

```sql
CALL GeoJsonRead('/home/user/data.geojson');
```

&rarr; Here `data.geojson` will produce a table named `data`.

```sql
CALL GeoJsonRead('/home/user/data.geojson.gz');
```

&rarr; Here `data.geojson.gz` will produce a table named `data_geojson`.

### 2. Case with `tableName`

```sql
CALL GeoJsonRead('/home/user/data.geojson', 'NEW_DATA');
```

&rarr; Here `data.geojson` will produce a table named `NEW_DATA`.

### 3. Case with `fileEncoding`

```sql
CALL GeoJsonRead('/home/user/data.geojson', 'NEW_DATA', 'utf-8');
```

### 4. Case with `deleteTable`

Load the `data.geojson` file
```sql
CALL GeoJsonRead('/home/user/data.geojson');
```

&rarr; the table `data` is created.

Now, load once again, using `deleteTable` = `true`

```sql
CALL GeoJsonRead('/home/user/data.geojson', true);
```

&rarr; the already existing `data` table is removed / replaced.

Now, load once again, using `deleteTable` = `false`

```sql
CALL GeoJsonRead('/home/user/data.geojson', false);
```

&rarr; Error message: `The table "DATA" already exists`.

## See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`ST_AsGeoJson`](../ST_AsGeoJson), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
