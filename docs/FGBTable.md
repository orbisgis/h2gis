# FGBTable

## Signatures

```sql
FGBTable(VARCHAR path);
FGBTable(VARCHAR path, VARCHAR columns);
```

## Description

Reads a FlatGeobuf file from `path` as a table, without importing it. `FGBTable` is used in the `FROM` clause of a query: the rows can be filtered, joined or aggregated directly, or only the useful columns and rows imported with `CREATE TABLE ... AS SELECT`.

The columns are the same as with [`FGBRead`](../FGBRead), without the `PK` column.

`columns` lists the columns to read, separated by commas, in the order of the result. The names are case insensitive, unless they are quoted. When `columns` is `NULL` or empty, all the columns are read. The geometry and the properties of a feature are stored together, so listing the columns does not make the reading much faster.

:::{note}
**The file is read at each query**

For repeated queries, import the file with [`FGBRead`](../FGBRead) or link it with [`FILE_TABLE`](../FILE_TABLE).
:::

## Examples

```sql
SELECT * FROM FGBTable('/home/user/countries.fgb');
```

```sql
SELECT * FROM FGBTable('/home/user/countries.fgb', 'NAME, THE_GEOM');
```

```sql
CREATE TABLE MY_TABLE AS
    SELECT * FROM FGBTable('/home/user/countries.fgb', 'NAME, THE_GEOM')
    WHERE NAME IS NOT NULL;
```

## See also

* [`FGBRead`](../FGBRead), [`FILE_TABLE`](../FILE_TABLE), [`GeoJsonTable`](../GeoJsonTable)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/fgb/FGBTable.java" target="_blank">Source code</a>
