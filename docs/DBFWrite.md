# DBFWrite

## Signatures

```sql
DBFWrite(VARCHAR path, VARCHAR tableName);
DBFWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
```

## Description

Writes the contents of table `tableName` to a [dBase][wiki] file
located at `path`.
The default value of `fileEncoding` is `ISO-8859-1`.

## Examples

```sql
-- Basic syntax:
CALL DBFWrite('/home/user/file.dbf', 'tableName');

-- Write a DBF file with UTF-8 encoding that was read with
-- ISO-8859-1 encoding:
CALL DBFWrite('/home/user/COMMUNE44.DBF', 'COMMUNE44iso-8859-1',
              'utf-8');

-- Read it back, using the encoding present in the header:
CALL DBFRead('/home/user/COMMUNE44.DBF', 'commune44');
SELECT * FROM commune44 LIMIT 2;
-- Answer:
-- |   NOM   | CODE_INSEE |      DEPART      |      REGION      |
-- |---------|------------|------------------|------------------|
-- | Puceul  |   44138    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
-- | Sévérac |   44196    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
```

## See also

* [`DBFRead`](../DBFRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/dbf/DBFWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/DBase
