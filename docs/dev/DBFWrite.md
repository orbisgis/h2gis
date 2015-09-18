---
layout: docs
title: DBFWrite
category: h2drivers
is_function: true
description: Table &rarr; DBF
prev_section: DBFRead
next_section: FILE_TABLE
permalink: /docs/dev/DBFWrite/
---

### Signatures

{% highlight mysql %}
DBFWrite(VARCHAR path, VARCHAR tableName);
DBFWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
{% endhighlight %}

### Description

Writes the contents of table `tableName` to a [dBase][wiki] file
located at `path`.
The default value of `fileEncoding` is `ISO-8859-1`.

### Examples

{% highlight mysql %}
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
{% endhighlight %}

##### See also

* [`DBFRead`](../DBFRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2drivers/src/main/java/org/h2gis/drivers/dbf/DBFWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/DBase
