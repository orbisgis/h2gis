---
layout: docs
title: TSVRead
category: h2drivers
is_function: true
description: TSV &rarr; Table
prev_section: ST_OSMDownloader
next_section: TSVWrite
permalink: /docs/dev/TSVRead/
---

### Signatures

{% highlight mysql %}
TSVRead(VARCHAR path);
TSVRead(VARCHAR path, BOOLEAN deleteTable);
TSVRead(VARCHAR path, VARCHAR tableName);
TSVRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
TSVRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
TSVRead(VARCHAR path, VARCHAR tableName, 
        VARCHAR fileEncoding, BOOLEAN deleteTable);
{% endhighlight %}

### Description

Reads the file specified by `path` as a Tab-Separated Values ([TSV][wiki]) file and copies its contents into a new table `tableName` in the database. 

This `.tsv` file may be zipped in a `.gz` file *(in this case, the `TSVRead` driver will unzip on the fly the `.gz` file)*. 

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If:

- the `tablename` parameter is not specified, then the resulting table has the same name as the TSV file.
- the `deleteTable` parameter is `true` and table `tableName` already exists in the database, then table `tableName` will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the table `tableName` already exists will be throwned.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

### Example

##### 1. Case with `path` and `tableName`

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

#### 2. Case with a `.gz` file

{% highlight mysql %}
CALL TSVRead('/home/user/GoT.tsv.gz');
{% endhighlight %}

&rarr; Here, since there is no `tableName` parameter, `GoT.tsv.gz` will produce a table named `GOT_TSV`.

#### 3. Case with `fileEncoding` 

{% highlight mysql %}
CALL TSVRead('/home/user/GoT.tsv', 'GameOfThrones', 'utf-8');
{% endhighlight %}

&rarr; Here the resulting `GameOfThrones` table is encoded in `utf-8`

#### 4. Case with `deleteTable`

Load the `GoT.tsv` file
{% highlight mysql %}
CALL TSVRead('/home/user/GoT.tsv');
{% endhighlight %}

&rarr; the table `GOT` is created.

Now, load once again, using `deleteTable` = `true`

{% highlight mysql %}
CALL TSVRead('/home/user/GoT.tsv', true);
{% endhighlight %}

&rarr; the already existing `GOT` table is removed / replaced.

Now, load once again, using `deleteTable` = `false`

{% highlight mysql %}
CALL TSVRead('/home/user/GoT.tsv', false);
{% endhighlight %}

&rarr; Error message: `The table "GOT" already exists`.


##### See also

* [`TSVWrite`](../TSVWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/tsv/TSVRead.java" target="_blank">Source code</a>

[wiki]: https://en.wikipedia.org/wiki/Tab-separated_values
