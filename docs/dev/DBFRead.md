---
layout: docs
title: DBFRead
category: h2drivers
is_function: true
description: DBF &rarr; Table
prev_section: CSVWrite
next_section: DBFWrite
permalink: /docs/dev/DBFRead/
---

### Signatures

{% highlight mysql %}
DBFRead(VARCHAR path, VARCHAR tableName);
DBFRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
{% endhighlight %}

### Description

Reads the file specified by `path` as a [dBase][wiki] file and
copies its contents into a new table `tableName` in the database.
Define `fileEncoding` to force encoding (useful when the header is
missing encoding information).

### Examples

{% highlight mysql %}
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
{% endhighlight %}

##### See also

* [`DBFWrite`](../DBFWrite)
* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/dbf/DBFRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/DBase
