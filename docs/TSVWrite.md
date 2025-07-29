# TSVWrite

## Signature

```sql
TSVWrite(VARCHAR path, VARCHAR tableName);
TSVWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);

TSVWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
TSVWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding, BOOLEAN deleteTable);
```

## Description

Save a table (`tablename`) into a Tab-Separated Values ([TSV][wiki]) file specified by `path`.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.

The `.tsv` file may be zipped in a `.gz` file *(in this case, the `TSVWrite` driver will zip on the fly the `.tsv` file)*. 

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If the `deleteTable` parameter is `true` and `path` file already exists, then `path` file will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `path` file already exists will be throwned.

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

### 1. Case with `path` and `tableName`

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

If you want to compress your resulting `.tsv` file into a `.gz` file, just execute

```sql
CALL TSVWrite('/home/user/GoT.tsv.gz', 'GameOfThrones');
```

As a result, you will obtain a `GoT.tsv.gz` file in which there is the `GoT.tsv` resulting file.

### 2. Case where `tableName` is the result of a selection

```sql
CALL TSVWrite('/home/user/GoT.tsv', 
    '(SELECT * FROM GAMEOFTHRONES WHERE NAME=''Stark'')');
```

Read it back:
```
CALL TSVRead('/home/user/GoT.tsv', 'GOT2');
SELECT * FROM GOT2;
```

Answer:
| NAME  | FIRSTNAME |   PLACE    |
|-------|-----------|------------|
| Stark | Arya      | Winterfell |


### 3. Case with `fileEncoding`

```sql
CALL TSVWrite('/home/user/GoT.tsv', 'GameOfThrones', 'utf-8');
```

### 4. Case with `deleteTable`

We condisder that the `Got.tsv` already exists here `/home/user/`

```sql
CALL TSVWrite('/home/user/GoT.tsv', 'GameOfThrones', true);
-- or
CALL TSVWrite('/home/user/GoT.tsv', 'GameOfThrones', 'utf-8', true);
```

Since we have `deleteTable` = `true`, the file `Got.tsv` is overwritten.

Now, execute with `deleteTable` = `false`
```sql
CALL TSVWrite('/home/user/GoT.tsv', 'GameOfThrones', false);
-- or
CALL TSVWrite('/home/user/GoT.tsv', 'GameOfThrones', 'utf-8', false);
```

An error message is throwned: `The tsv file already exists`

## See also

* [`TSVRead`](../TSVRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/tsv/TSVWrite.java" target="_blank">Source code</a>

[wiki]: https://en.wikipedia.org/wiki/Tab-separated_values
