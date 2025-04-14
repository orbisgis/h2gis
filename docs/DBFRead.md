# DBFRead

## Signatures

```sql
DBFRead(VARCHAR path);
DBFRead(VARCHAR path, VARCHAR tableName);
DBFRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
```

## Description

Reads the file specified by `path` as a [dBase][wiki] file and
copies its contents into a new table `tableName` in the database.
Define `fileEncoding` to force encoding (useful when the header is
missing encoding information).

If the `tablename` parameter is not specified, then the resulting table has the same name as the dBase file.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

## Examples

```sql
-- Basic syntax:
CALL DBFRead('/home/user/file.dbf', 'tableName');

-- In the next two examples, we show what happens when we attempt to
-- read a DBF file with the wrong encoding, and how to fix it. Here
-- UTF-8 doesn't understand accented characters, so "Sévérac" is
-- displayed as "S".
CALL DBFRead('/home/user/COMMUNE.DBF', 'commune44utf', 'utf-8');
SELECT * FROM commune44utf LIMIT 2;
-- Answer:
-- |  NOM   | CODE_INSEE |      DEPART      |      REGION      |
-- |--------|------------|------------------|------------------|
-- | Puceul |   44138    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
-- | S      |   44196    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |

-- To fix this problem, we specify the right encoding:
CALL DBFRead('/home/user/COMMUNE.DBF', 'commune44iso',
             'iso-8859-1');
SELECT * FROM commune44iso LIMIT 2;
-- Answer:
-- |   NOM   | CODE_INSEE |      DEPART      |      REGION      |
-- |---------|------------|------------------|------------------|
-- | Puceul  |   44138    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
-- | Sévérac |   44196    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
```

## See also

* [`DBFWrite`](../DBFWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/dbf/DBFRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/DBase
