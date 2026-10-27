# DBFTable

## Signatures

```sql
DBFTable(VARCHAR path);
DBFTable(VARCHAR path, VARCHAR columns);
DBFTable(VARCHAR path, VARCHAR columns, VARCHAR fileEncoding);
```

## Description

Reads a dBase file from `path` as a table, without importing it. `DBFTable` is used in the `FROM` clause of a query: the rows can be filtered, joined or aggregated directly, or only the useful columns and rows imported with `CREATE TABLE ... AS SELECT`.

The columns are the same as with [`DBFRead`](../DBFRead), without the `PK` column.

`columns` lists the columns to read, separated by commas, in the order of the result. The names are case insensitive, unless they are quoted.

Define `fileEncoding` to force the encoding of the file (useful when the file does not give it, e.g. `CP1252`, `ISO-8859-1`); `columns` can then be `NULL` or empty.

:::{note}
**The file is read at each query**

For repeated queries, import the file with [`DBFRead`](../DBFRead) or link it with [`FILE_TABLE`](../FILE_TABLE).
:::

## Examples

```sql
SELECT * FROM DBFTable('/home/user/building.dbf');
```

```sql
SELECT * FROM DBFTable('/home/user/building.dbf', 'ID, HEIGHT');
```

```sql
CREATE TABLE MY_TABLE AS
    SELECT * FROM DBFTable('/home/user/building.dbf', 'ID, HEIGHT')
    WHERE HEIGHT IS NOT NULL;
```

```sql
SELECT * FROM DBFTable('/home/user/building.dbf', '', 'CP1252');
```

## See also

* [`DBFRead`](../DBFRead), [`FILE_TABLE`](../FILE_TABLE), [`GeoJsonTable`](../GeoJsonTable)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/dbf/DBFTable.java" target="_blank">Source code</a>
