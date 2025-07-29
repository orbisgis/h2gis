# DBFRead

## Signatures

```sql
DBFRead(VARCHAR path);
DBFRead(VARCHAR path, BOOLEAN deleteTable);

DBFRead(VARCHAR path, VARCHAR tableName);
DBFRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);

DBFRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
DBFRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding, BOOLEAN deleteTable);
```

## Description

Reads the file specified by `path` as a [dBase][wiki] file and copies its contents into a new table `tableName` in the database.

A new column named `PK`, storing a primary key (`INT` value), is added. If the input `.dbf` has already a `PK` column then the added column is named `PK2` *(and so on)*.

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If:

- the `tablename` parameter is not specified, then the resulting table has the same name as the dBase file.
- the `deleteTable` parameter is `true` and table `tableName` already exists in the database, then table `tableName` will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the table `tableName` already exists will be throwned.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

## Examples

In following example, we have a DBF file, which is stored here : `/home/user/city.dbf`. This file is structured as follow.

```sql
NAME   ID
Vannes   56260
Theix   56251
Bréhan   56206
```

### 1. Case with `path`

```sql
CALL DBFRead('/home/user/city.dbf');
```

Returns the following table `CITY`. A column `PK` has been added.

| PK |   NAME    |  ID   |
|:--:|:---------:|:-----:|
| 1  | Vannes    | 56260 |
| 2  | Theix     | 56251 |
| 3  | Bréhan | 56206 |

### 2. Case with `tableName`

```sql
CALL DBFRead('/home/user/city.dbf', 'MYCITY');
```

Returns the table `MYCITY`

### 3. Case with `fileEncoding` 

In the next two examples, we show what happens when we attempt to read a DBF file with the wrong encoding, and how to fix it. 
Here UTF-8 doesn't understand accented characters, so "`Bréhan`" is displayed as "`Br`".

```sql
CALL DBFRead('/home/user/city.dbf', 'CITY', 'utf-8');
```

| PK |  NAME  |  ID   |
|----|--------|-------|
| 1  | Vannes | 56260 |
| 2  | Theix  | 56251 |
| 3  | Br     | 56206 |


To fix this problem, we specify the right encoding (`iso-8859-1`):

```sql
CALL DBFRead('/home/user/city.dbf', 'CITY', 'iso-8859-1');
```

| PK |  NAME  |  ID   |
|----|--------|-------|
| 1  | Vannes | 56260 |
| 2  | Theix  | 56251 |
| 3  | Bréhan | 56206 |


### 4. Case with `deleteTable`

Load the `city.dbf` file
```sql
CALL DBFRead('/home/user/city.dbf');
```

&rarr; the table `CITY` is created.

Now, load once again, using `deleteTable` = `true`

```sql
CALL DBFRead('/home/user/city.dbf', true);
```

&rarr; the already existing `CITY` table is removed / replaced.

Now, load once again, using `deleteTable` = `false`

```sql
CALL DBFRead('/home/user/city.dbf', false);
```

&rarr; Error message: `The table "CITY" already exists`.


## See also

* [`DBFWrite`](../DBFWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/dbf/DBFRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/DBase
