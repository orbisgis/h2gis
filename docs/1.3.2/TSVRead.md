---
layout: docs
title: TSVRead
category: h2drivers
is_function: true
description: TSV &rarr; Table
prev_section: ST_OSMDownloader
next_section: TSVWrite
permalink: /docs/1.3.2/TSVRead/
---

### Signatures

{% highlight mysql %}
TSVRead(VARCHAR path);
TSVRead(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Reads the file specified by `path` as a Tab-Separated Values ([TSV][wiki]) file and
copies its contents into a new table `tableName` in the database.

If the `tablename` parameter is not specified, then the resulting table has the same name as the TSV file.

### Example

In following example, we have a TSV file, which is stored here : `/home/user/GoT.tsv`. This file is structured as follow.

{% highlight mysql %}
NAME	FIRSTNAME	PLACE
Stark	Arya	Winterfell
Lannister	Tyrion	Westeros
Snow	Jon	Castle Black
Baelish	Peter	King's Landing
{% endhighlight mysql %}

Now we can convert this file into a table

{% highlight mysql %}
CALL TSVRead('/home/user/GoT.tsv', 'GameOfThrones');
SELECT * FROM GameOfThrones ;
-- Answer:
-- |   NAME    | FIRSTNAME |     PLACE      |
-- |-----------|-----------|----------------|
-- | Stark     | Arya      | Winterfell     |
-- | Lannister | Tyrion    | Westeros       |
-- | Snow      | Jon       | Castle Black   |
-- | Baelish   | Peter     | King's Landing |
{% endhighlight %}

##### See also

* [`TSVWrite`](../TSVWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/tsv/TSVRead.java" target="_blank">Source code</a>

[wiki]: https://en.wikipedia.org/wiki/Tab-separated_values
