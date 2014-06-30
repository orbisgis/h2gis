---
layout: docs
title: CSVRead
category: h2drivers
is_function: true
description: CSV &rarr; Table
prev_section: h2drivers
next_section: CSVWrite
permalink: /docs/dev/CSVRead/
---

### Signatures

{% highlight mysql %}
CSVRead(VARCHAR path);
CSVRead(VARCHAR path, VARCHAR columnNameHeader,
        VARCHAR stringDecode);
{% endhighlight %}

### Description

<div class="note">
  <h5>This function is a part of H2.</h5>
  <p>Please first consult its
  <a href="http://www.h2database.com/html/functions.html#csvread"
  target="_blank">documentation</a> on the H2 website.</p>
</div>

Reads a CSV file.
All columns are of type `VARCHAR`.

Optional variable `columnNameHeader` is a list of column names
separated by the field separator. If `NULL`, the first line of the
file is interpreted as the column names.

{% include stringDecode.html %}

### Examples

{% highlight mysql %}
-- A ,-separated file:
CREATE TABLE AREA AS
    SELECT * FROM CSVRead('/home/user/area.csv') LIMIT 2;
-- Answer:
-- |                 THE_GEOM                 |   ID   |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |
{% endhighlight %}

{% highlight mysql %}
-- A ;-separated file containing the column names on the first line:
CREATE TABLE AREA2 AS
    SELECT * FROM CSVRead('/home/user/area.csv',
                          NULL,
                          'fieldSeparator=;') LIMIT 2;
-- Answer:
-- |                  THE_GEOM                |   ID   |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |
{% endhighlight %}

{% highlight mysql %}
-- A ;-separated file with no column names on the first line:
CREATE TABLE AREA2 AS
    SELECT * FROM CSVRead('/home/user/area.csv',
                          'COLUMN1; COLUMN2',
                          'fieldSeparator=;') LIMIT 2;
-- Answer:
-- |                  COLUMN1                 | COLUMN2 |
-- | ---------------------------------------- | ------- |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |       1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |       2 |
{% endhighlight %}

##### See also

* [`CSVWrite`](../CSVWrite)
* H2 <a href="http://www.h2database.com/html/functions.html#csvread"
target="_blank">CSVRead</a>
