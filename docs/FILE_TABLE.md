# FILE_TABLE

## Signature

```sql
FILE_TABLE(VARCHAR path, VARCHAR tableName);
```

## Description

Uses an appropriate driver to open the file at `path` and create a
linked (read-only) table `tableName`.
This table is always in-sync with the source file.

Currently supported:

* [shapefile][wikishp] (`.shp`)
* [dBase][wikidbf] file (`.dbf`)
* [flatgeobuffer](https://flatgeobuf.org/) (`.fgb`)

<div class="note warning">
  <h5>If the source file is moved or deleted, the special table will still
  exist but will be empty.</h5>
</div>

## Examples

### Basic syntax

```sql
CALL FILE_TABLE('/home/user/myshapefile.shp', 'tableName');
CALL FILE_TABLE('/home/user/dbase.dbf', 'tableName');
CALL FILE_TABLE('/home/user/myflatgeobuffer.fgb', 'tableName');
```

### Auto detect

The following two examples show that the driver to be used is automatically detected from the file extension:

```sql
CALL FILE_TABLE('/home/user/COMMUNE.DBF', 'commune');
SELECT * FROM commune LIMIT 2;
```

Answer:

|   NAME  |    CODE    |
|---------|------------|
| Puceul  |   44138    |
| Sévérac |   44196    |

```sql
CALL FILE_TABLE('/home/user/COMMUNE.SHP', 'commune44');
SELECT * FROM commune44 LIMIT 2;
```

Answer:

|                 THE_GEOM                  |   NOM   | CODE  |
| ----------------------------------------- | ------- |-------|
| MULTIPOLYGON(((350075.2 6719771.8, ...  350075.2 6719771.8))) | Puceul  | 44138 |
| MULTIPOLYGON(((317341.5 6727021, ...  317341.5 6727021))) | Sévérac | 44196 |



## See also

* [`SHPRead`](../SHPRead), [`DBFRead`](../DBFRead), [`FGBRead`](../FGBRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/DriverManager.java" target="_blank">Source code</a>

[wikidbf]: http://en.wikipedia.org/wiki/DBase
[wikishp]: http://en.wikipedia.org/wiki/Shapefile
