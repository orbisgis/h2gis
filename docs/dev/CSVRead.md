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
tableName[*]CSVRead(VARCHAR fileName);
tableName[*]CSVRead(VARCHAR fileName, VARCHAR columnNameHeader,
         varchar stringDecode);
{% endhighlight %}

### Description
Reads a CSV file.
By default the stringDecode is:

* charset =UTF-8
* fieldDelimiter ="
* fieldSeparator =,
* lineSeparator =\n
* writeColumnHeader =true

### Examples

#### CSV field separator is `, `

{% highlight mysql %}
create table area as select * from CSVRead('/home/Documents/
                                            area.csv');
-- Answer:
-- |                 the_geom                 | idarea |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |
{% endhighlight %}

#### CSV field separator is `;` and contains the column header

{% highlight mysql %}
CREATE TABLE area2 AS SELECT * FROM CSVRead(
   '/home/Documents/area.csv', null,
   'fieldSeparator=;');
-- Answer:
-- |                     THE_GEOM             | IDAREA |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |
{% endhighlight %}

#### CSV field separator is `;` and don't contains the column header

{% highlight mysql %}
CREATE TABLE area2 AS SELECT * FROM CSVRead(
   '/home/Documents/area.csv', 'column1; column2',
   'fieldSeparator=;');
-- Answer:
-- |                     column1              | column2 |
-- | ---------------------------------------- | ------- |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |       1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |       2 |
{% endhighlight %}

##### See also

* [`CSVWrite`](../CSVWrite)
* <a href="http://www.h2database.com/html/grammar.html#csv_options"
target="_blank">H2 grammar</a>
