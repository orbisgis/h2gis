# TSVWrite

## Signature

```sql
TSVWrite(VARCHAR path, VARCHAR tableName);
```

## Description

Save a table (`tablename`) into a Tab-Separated Values ([TSV][wiki]) file specified by `path`.

## Example

In following example, we have a table called `GameOfThrones` and structured as follow.

```sql
SELECT * FROM GameOfThrones;
```
Answer:
|   NAME    | FIRSTNAME |     PLACE      |
|-----------|-----------|----------------|
| Stark     | Arya      | Winterfell     |
| Lannister | Tyrion    | Westeros       |
| Snow      | Jon       | Castle Black   |
| Baelish   | Peter     | King's Landing |


Now we save this table into a .tsv file ...
```sql
CALL TSVWrite('/home/user/GoT.tsv', 'GameOfThrones');
```

... and we can open this `GoT.tsv` file in a text editor

```text
NAME	FIRSTNAME	PLACE
Stark	Arya	Winterfell
Lannister	Tyrion	Westeros
Snow	Jon	Castle Black
Baelish	Peter	King's Landing
```

## See also

* [`TSVRead`](../TSVRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/tsv/TSVWrite.java" target="_blank">Source code</a>

[wiki]: https://en.wikipedia.org/wiki/Tab-separated_values
