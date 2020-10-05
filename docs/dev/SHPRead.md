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

Reads the file specified by `path` as a [shapefile][wiki] and copies its
contents into a new table `tableName` in the database.
Define `fileEncoding` to force encoding (useful when the header is
missing encoding information).

If:

- the `tableName` parameter is not specified, then the resulting table has the same name as the shapefile.
- the `deleteTable` parameter is `true` and table `tableName` already exists in the database, then table `tableName` will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the table `tableName` already exists will be throwned.

<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

### Examples

{% highlight mysql %}
-- Basic syntax:
CALL SHPRead('/home/user/file.shp', 'tableName');

-- In the next two examples, we show what happens when we attempt
-- to read a SHP file with the wrong encoding, and how to fix it.
-- Here UTF-8 doesn't understand accented characters, 
-- so "Sévérac" is displayed as "S".
CALL SHPRead('/home/user/COMMUNE.SHP', 'commune44utf',
             'utf-8');
SELECT * FROM commune44utf LIMIT 2;
-- Answer:
-- |                 THE_GEOM                  |   NOM   |
-- | ----------------------------------------- | ------- |
-- | MULTIPOLYGON(((350075.2 6719771.8,        | Puceul  |
-- |   350072.7 6719775.5, 350073 6719780.7,   |         |
-- |   350075.2 6719771.8)))                   |         |
-- | MULTIPOLYGON(((317341.5 6727021,          | S       |
-- |   317309.9 6727036.8, 317193.3 6727066.5, |         |
-- |   317341.5 6727021)))                     |         |

-- To fix this problem, we specify the right encoding:
CALL SHPRead('/home/user/COMMUNE.SHP', 'commune44iso',
             'iso-8859-1');
SELECT * FROM commune44iso LIMIT 2;
-- Answer:
-- |                 THE_GEOM                  |   NOM   |
-- | ----------------------------------------- | ------- |
-- | MULTIPOLYGON(((350075.2 6719771.8,        | Puceul  |
-- |   350072.7 6719775.5, 350073 6719780.7,   |         |
-- |   350075.2 6719771.8)))                   |         |
-- | MULTIPOLYGON(((317341.5 6727021,          | Sévérac |
-- |   317309.9 6727036.8, 317193.3 6727066.5, |         |
-- |   317341.5 6727021)))                     |         |
{% endhighlight %}

#### Using the `deleteTable` parameter

##### 1- Import the `COMMUNE.shp` layer into the `COMMUNE` table
{% highlight mysql %}
CALL SHPRead('/home/user/COMMUNE.shp', 'COMMUNE');
{% endhighlight %}

##### 2- Now, import once again `COMMUNE.shp`, using `deleteTable`=`true`
{% highlight mysql %}
CALL SHPRead('/home/user/COMMUNE.shp', 'COMMUNE', true);
{% endhighlight %}
Returns : `null` (= no errors, the table `COMMUNE` has been replaced).

##### 3- Then, import once again `COMMUNE.shp`, using `deleteTable`=`false`
{% highlight mysql %}
CALL SHPRead('/home/user/COMMUNE.shp', 'COMMUNE', false);
{% endhighlight %}
Returns : `The table "COMMUNE" already exists`

##### See also

* [`SHPWrite`](../SHPWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/shp/SHPRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Shapefile
