# FGBWrite

## Signatures

```sql
FGBWrite(VARCHAR path, VARCHAR tableName);
FGBWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
FGBWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable, VARCHAR options);
```

## Description

Writes the contents of table `tableName` to a [FlatGeobuf](https://flatgeobuf.org/) file located at `path`.

`tableName` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.


If the `deleteTable` parameter is `true` and `path` file already exists, then `path` file will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `path` file already exists will be throwned.

With `options`, it is possible to define:
* whether you want to create a spatial index (RTree). If so, writes `'createIndex=true'`
* the index node size (noted `nodeSize`), which represents *"the branching factor of the RTree used for the flatgeobuf spatial index, i.e. the number of child nodes under each interior node in the tree"* ([source](https://worace.works/2022/03/12/flatgeobuf-implementers-guide/)). By default `nodeSize` = `16`. This parameter has to be used in combination with `createIndex` &rarr; `'createIndex=true nodeSize=16'`

## Examples

In the following example, we are working with a table named `TEST` and defined as follow.

```sql
CREATE TABLE TEST(ID INT PRIMARY KEY, THE_GEOM GEOMETRY(POINT));
INSERT INTO TEST VALUES (1, 'POINT(0 1)');
INSERT INTO TEST VALUES (2, 'POINT(2 4)');
```

### 1. Case with `path` and `tableName`

```sql
-- Write a spatial table to a FlatGeobuf file:
CALL FGBWrite('/home/user/test.fgb', 'TEST');

-- Read it back:
CALL FGBread('/home/user/test.fgb', 'TEST2');
SELECT * FROM TEST2;
```

Answer:
| ID | THE_GEOM    |
|----|-------------|
| 1  | POINT(0 1)  |
| 2  | POINT(2 4)  |


### 2. Case where `tableName` is the result of a selection

```sql
CALL FGBWrite('/home/user/test.fgb', '(SELECT * FROM TEST WHERE ID<2 )');
-- Read it back:
CALL FGBRead('/home/user/test.fgb', 'TEST2');
SELECT * FROM TEST2;
```

Answer:

| ID | THE_GEOM    |
|----|-------------|
| 1  | POINT(0 1)  |


### 3. Case with `deleteTable`

Export `TEST` table to `test.fgb` file
```sql
CALL FGBWrite('/home/user/test.fgb', 'TEST');
```

&rarr; the file `test.fgb` is created.

Now, write it once again, using `deleteTable` = `TRUE`

```sql
CALL FGBWrite('/home/user/test.fgb', 'TEST', TRUE);
```

&rarr; the already existing `test.fgb` file is removed / replaced.

Now, write once again, using `deleteTable` = `FALSE`

```sql
CALL FGBWrite('/home/user/test.fgb', 'TEST', FALSE);
```

&rarr; Error message: `The FlatGeobuf file already exist`.


### 3. Case with `deleteTable` and `options`

```sql
CALL FGBWrite('/home/user/test.fgb', 'TEST', TRUE, 'createIndex=true');
```
or

```sql
CALL FGBWrite('/home/user/test.fgb', 'TEST', TRUE, 'createIndex=true nodeSize=16');
```

## See also

* [`FGBRead`](../FGBRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/fgb/FGBWrite.java" target="_blank">Source code</a>