---
layout: docs
title: FILE_TABLE
category: h2drivers
is_function: true
description: Link a table to a file
prev_section: DBFWrite
next_section: GPXRead
permalink: /docs/dev/FILE_TABLE/
---

### Signature

{% highlight mysql %}
FILE_TABLE(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Uses an appropriate driver to open the file at `path` and create a
linked (read-only) table `tableName`.
This table is always in-sync with the source file.

Currently supported:

* [shapefile][wikishp] (`.shp`)
* [dBase][wikidbf] file (`.dbf`)

<div class="note warning">
  <h5>If the source file is moved or deleted, the special table will still
  exist but will be empty.</h5>
</div>

### Examples

{% highlight mysql %}
-- Basic syntax:
CALL FILE_TABLE('/home/user/myshapefile.shp', 'tableName');
CALL FILE_TABLE('/home/user/dbase.dbf', 'tableName');

-- The next two examples show that which driver to use is detected
-- automatically from the file extension:
CALL FILE_TABLE('/home/user/COMMUNE.DBF', 'commune');
SELECT * FROM commune LIMIT 2;
-- Answer:
-- |   NOM   | CODE_INSEE |      DEPART      |      REGION      |
-- |---------|------------|------------------|------------------|
-- | Puceul  |   44138    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
-- | Sévérac |   44196    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |

CALL FILE_TABLE('/home/user/COMMUNE.SHP', 'commune44');
SELECT * FROM commune44 LIMIT 2;
-- Answer:
-- |                   geom                    |   NOM   |
-- | ----------------------------------------- | ------- |
-- | MULTIPOLYGON(((350075.2 6719771.8,        | Puceul  |
-- |   350072.7 6719775.5, 350073 6719780.7,   |         |
-- |   350075.2 6719771.8)))                   |         |
-- | MULTIPOLYGON(((317341.5 6727021,          | Sévérac |
-- |   317309.9 6727036.8, 317193.3 6727066.5, |         |
-- |   317341.5 6727021)))                     |         |
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/DriverManager.java" target="_blank">Source code</a>

[wikidbf]: http://en.wikipedia.org/wiki/DBase
[wikishp]: http://en.wikipedia.org/wiki/Shapefile
