---
layout: docs
title: SHPRead
category: h2drivers
is_function: true
description: SHP &rarr; Table
prev_section: KMLWrite
next_section: SHPWrite
permalink: /docs/dev/SHPRead/
---

### Signatures

{% highlight mysql %}
SHPRead(VARCHAR path);
SHPRead(VARCHAR path, BOOLEAN deleteTable);
SHPRead(VARCHAR path, VARCHAR tableName);
SHPRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
SHPRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
SHPRead(VARCHAR path, VARCHAR tableName, 
        VARCHAR fileEncoding, BOOLEAN deleteTable);
{% endhighlight %}

### Description

Reads the file specified by `path` as a [shapefile][wiki] and copies its contents into a new table `tableName` in the database.

A new column named `PK`, storing a primary key (`INT` value), is added. If the input `.shp` has already a `PK` column then the added column is named `PK2` *(and so on)*.

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

If:

- the `tableName` parameter is not specified, then the resulting table has the same name as the shapefile.
- the `deleteTable` parameter is `true` and table `tableName` already exists in the database, then table `tableName` will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the table `tableName` already exists will be throwned.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

### Examples

In following example, we have a SHP file, which is stored here : `/home/user/city.shp`. This file is structured as follow.

|      THE_GEOM         |  NAME  |   ID  |
|:---------------------:|:------:|:-----:|
| MULTIPOLYGON(((...))) | Vannes | 56260 |
| MULTIPOLYGON(((...))) | Theix  | 56251 |
| MULTIPOLYGON(((...))) | Bréhan | 56024 |

#### 1. Case with `path`

{% highlight mysql %}
CALL SHPRead('/home/user/city.shp');
{% endhighlight %}

The table `CITY` is created and a new `PK` column is added:

| PK |      THE_GEOM         |  NAME  |   ID  |
|:--:|:---------------------:|:------:|:-----:|
| 1  | MULTIPOLYGON(((...))) | Vannes | 56260 |
| ... | ... | ... | ... |

#### 2. Case with `path` and `tableName`

{% highlight mysql %}
CALL SHPRead('/home/user/city.shp', 'MyCity');
{% endhighlight %}

The table `MYCITY` is created.

#### 3. Case with `fileEncoding`

In the next two examples, we show what happens when we attempt to read a SHP file with the wrong encoding, and how to fix it.
Here UTF-8 doesn't understand accented characters, so the city named `Bréhan` is displayed as `Br`.

{% highlight mysql %}
CALL SHPRead('/home/user/city.shp', 'CITYutf', 'utf-8');
SELECT * FROM CITYutf;
-- Answer:
-- | PK |        THE_GEOM       |  NAME  |  ID   |
-- |----| --------------------- | ------ | ----- |
-- | 1  | MULTIPOLYGON(((...))) | Vannes | 56260 |
-- | 2  | MULTIPOLYGON(((...))) | Theix  | 56251 |
-- | 3  | MULTIPOLYGON(((...))) | Br     | 56024 |

{% endhighlight %}
To fix this problem, we specify the right encoding (`iso-8859-1`):

{% highlight mysql %}
CALL SHPRead('/home/user/city.shp', 'CITYiso', 'iso-8859-1');
SELECT * FROM CITYiso;
-- Answer:
-- | PK |        THE_GEOM       |  NAME  |  ID   |
-- |----| --------------------- | ------ | ----- |
-- | 1  | MULTIPOLYGON(((...))) | Vannes | 56260 |
-- | 2  | MULTIPOLYGON(((...))) | Theix  | 56251 |
-- | 3  | MULTIPOLYGON(((...))) | Bréhan | 56024 |
{% endhighlight %}

#### 4. Case with `deleteTable`

Import the `city.shp` layer into the `CITY` table
{% highlight mysql %}
CALL SHPRead('/home/user/CITY.shp', 'CITY');
{% endhighlight %}

Now, import once again `city.shp`, using `deleteTable`=`true`
{% highlight mysql %}
CALL SHPRead('/home/user/city.shp', 'CITY', true);
{% endhighlight %}
Returns : `null` (= no errors, the table `CITY` has been replaced).

Then, import once again `city.shp`, using `deleteTable`=`false`
{% highlight mysql %}
CALL SHPRead('/home/user/city.shp', 'CITY', false);
{% endhighlight %}
Returns : `The table "CITY" already exists`

##### See also

* [`SHPWrite`](../SHPWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/shp/SHPRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Shapefile
