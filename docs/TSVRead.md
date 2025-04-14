# TSVRead

## Signatures

```sql
TSVRead(VARCHAR path);
TSVRead(VARCHAR path, VARCHAR tableName);
```

## Description

Reads the file specified by `path` as a Tab-Separated Values ([TSV][wiki]) file and
copies its contents into a new table `tableName` in the database.

If the `tablename` parameter is not specified, then the resulting table has the same name as the TSV file.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

## Example

In following example, we have a TSV file, which is stored here : `/home/user/GoT.tsv`. This file is structured as follow.

```text
NAME	FIRSTNAME	PLACE
Stark	Arya	Winterfell
Lannister	Tyrion	Westeros
Snow	Jon	Castle Black
Baelish	Peter	King's Landing
```

Now we can convert this file into a table

```sql
CALL TSVRead('/home/user/GoT.tsv', 'GameOfThrones');
SELECT * FROM GameOfThrones ;
```
Answer:

|   NAME    | FIRSTNAME |     PLACE      |
|-----------|-----------|----------------|
| Stark     | Arya      | Winterfell     |
| Lannister | Tyrion    | Westeros       |
| Snow      | Jon       | Castle Black   |
| Baelish   | Peter     | King's Landing |


## See also

* [`TSVWrite`](../TSVWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/tsv/TSVRead.java" target="_blank">Source code</a>

[wiki]: https://en.wikipedia.org/wiki/Tab-separated_values
