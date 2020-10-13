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


#### Basic syntax
{% highlight mysql %}
CALL FILE_TABLE('/home/user/myshapefile.shp', 'tableName');
CALL FILE_TABLE('/home/user/dbase.dbf', 'tableName');
{% endhighlight %}

#### Auto detect
The next two examples show that the driver to use is automatically detected from the file extension:

{% highlight mysql %}
CALL FILE_TABLE('/home/user/CITY.DBF', 'citydbf');
SELECT * FROM citydbf LIMIT 2;
-- Answer:
-- |  NAME   | CODE  |
-- |---------|-------|
-- | Puceul  | 44138 |
-- | Sévérac | 44196 |

CALL FILE_TABLE('/home/user/CITY.SHP', 'cityshp');
SELECT * FROM cityshp LIMIT 2;
-- Answer:
-- |                 THE_GEOM                 |  NAME   | CODE  |
-- | ---------------------------------------- | ------- | ----- |
-- | MULTIPOLYGON(((350075.2 6719771.8,       | Puceul  | 44138 |
-- |  350072.7 6719775.5, 350073 6719780.7,   |         |       |
-- |  350075.2 6719771.8)))                   |         |       |
-- | MULTIPOLYGON(((317341.5 6727021,         | Sévérac | 44196 |
-- |  317309.9 6727036.8, 317193.3 6727066.5, |         |       |
-- |  317341.5 6727021)))                     |         |       |
{% endhighlight %}

##### See also

* [`SHPRead`](../SHPRead), [`DBFRead`](../DBFRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/DriverManager.java" target="_blank">Source code</a>

[wikidbf]: http://en.wikipedia.org/wiki/DBase
[wikishp]: http://en.wikipedia.org/wiki/Shapefile
