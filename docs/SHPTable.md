# SHPTable

## Signatures

```sql
SHPTable(VARCHAR path);
SHPTable(VARCHAR path, VARCHAR columns);
SHPTable(VARCHAR path, VARCHAR columns, VARCHAR fileEncoding);
```

## Description

Reads a shapefile from `path` as a table, without importing it. `SHPTable` is used in the `FROM` clause of a query: the rows can be filtered, joined or aggregated directly, or only the useful columns and rows imported with `CREATE TABLE ... AS SELECT`.

The columns are the same as with [`SHPRead`](../SHPRead), without the `PK` column.

`columns` lists the columns to read, separated by commas, in the order of the result. The names are case insensitive, unless they are quoted. When `columns` is `NULL` or empty, all the columns are read. Reading only some columns is faster: when `THE_GEOM` is not listed, the `.shp` file is not read at all.

Define `fileEncoding` to force the encoding of the attributes (useful when the file does not give it, e.g. `CP1252`, `ISO-8859-1`).

:::{note}
**The file is read at each query**

For repeated queries, import the file with [`SHPRead`](../SHPRead) or link it with [`FILE_TABLE`](../FILE_TABLE).
:::

## Examples

```sql
SELECT * FROM SHPTable('/home/user/building.shp');
```

```sql
SELECT * FROM SHPTable('/home/user/building.shp', 'THE_GEOM, HEIGHT');
```

```sql
CREATE TABLE MY_TABLE AS
    SELECT * FROM SHPTable('/home/user/building.shp', 'THE_GEOM, HEIGHT')
    WHERE HEIGHT IS NOT NULL;
```

```sql
SELECT * FROM SHPTable('/home/user/building.shp', '', 'CP1252');
```

## See also

* [`SHPRead`](../SHPRead), [`FILE_TABLE`](../FILE_TABLE), [`GeoJsonTable`](../GeoJsonTable)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/shp/SHPTable.java" target="_blank">Source code</a>
