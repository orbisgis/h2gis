# DBFWrite

## Signatures

```sql
DBFWrite(VARCHAR path, VARCHAR tableName);
DBFWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);

DBFWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
DBFWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding, BOOLEAN deleteTable);
```

## Description

Writes the contents of table `tableName` to a [dBase][wiki] file located at `path`.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If the `deleteTable` parameter is `true` and `path` file already exists, then `path` file will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `path` file already exists will be throwned.

## Examples

In following example, we have a table called `CITY` and structured as follow.

```sql
CREATE TABLE CITY (NAME VARCHAR, ID INT);
INSERT INTO CITY VALUES ('Vannes', 56260),
                        ('Theix', 56251), 
                        ('Saint-Avé', 56206);
```
### 1. Case with `tableName`

Write the .dbf file ...
```sql
CALL DBFWrite('/home/user/city.dbf', 'CITY');
```

... and read it back

```sql
CALL DBFRead('/home/user/city.dbf', 'CITY2');
SELECT * FROM CITY2;
```

| PK |   NAME    |  ID   |
|----|-----------|-------|
| 1  | Vannes    | 56260 |
| 2  | Theix     | 56251 |
| 3  | Saint-Avé | 56206 |


### 2. Case where `tableName` is the result of a selection

```sql
CALL DBFWrite('/home/user/city.dbf', 
              '(SELECT * FROM CITY WHERE NAME=''Vannes'')');
```

... and read it back
```sql
CALL DBFRead('/home/user/city.dbf', 'CITY2');
SELECT * FROM CITY2;
```

Answer:

| PK |   NAME    |  ID   |
|----|-----------|-------|
| 1  | Vannes    | 56260 |


### 3. Case with `fileEncoding`

```sql
CALL DBFWrite('/home/user/city.dbf', 'CITY', 'utf-8');
```

### 4. Case with `deleteTable`
We condisder that the `city.dbf` already exists here `/home/user/`
```sql
CALL DBFWrite('/home/user/city.dbf', 'CITY', true);
-- or
CALL DBFWrite('/home/user/city.dbf', 'CITY', 'utf-8', true);
```
Since we have `deleteTable` = `true`, the file `city.dbf` is overwritten.

Now, execute with `deleteTable` = `false`

```sql
CALL DBFWrite('/home/user/city.dbf', 'CITY', false);
-- or
CALL DBFWrite('/home/user/city.dbf', 'CITY', 'utf-8', false);
```

An error message is throwned: `The dbf file already exists`

## See also

* [`DBFRead`](../DBFRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/dbf/DBFWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/DBase
