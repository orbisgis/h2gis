# FGBRead

## Signatures

```sql
FGBRead(VARCHAR path, VARCHAR tableName);
FGBRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
```

## Description

Reads a [FlatGeobuf](https://flatgeobuf.org/) file from `path` and creates the corresponding spatial table `tableName`. 

If the `deleteTable` parameter is `true` and table `tableName` already exists in the database, then table `tableName` will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the table `tableName` already exists will be throwned.

## Examples

### 1. Case with `tableName`

```sql
CALL FGBRead('/home/user/data.fgb', 'NEW_DATA');
```

&rarr; Here `data.fgb` will produce a table named `NEW_DATA`.


### 2. Case with `tableName` and `deleteTable`

Load the `data.fgb` file
```sql
CALL FGBRead('/home/user/data.fgb', 'NEW_DATA');
```

&rarr; the table `NEW_DATA` is created.

Now, load once again, using `deleteTable` = `true`

```sql
CALL FGBRead('/home/user/data.fgb', 'NEW_DATA',  true);
```

&rarr; the already existing `NEW_DATA` table is removed / replaced.

Now, load once again, using `deleteTable` = `false`

```sql
CALL FGBRead('/home/user/data.fgb', 'NEW_DATA', false);
```

&rarr; Error message: `The table "NEW_DATA" already exists`.

## See also

* [`FGBWrite`](../FGBWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/fgb/FGBRead.java" target="_blank">Source code</a>