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

##### Separated file
{% highlight mysql %}
CREATE TABLE AREA AS
    SELECT * FROM CSVRead('/home/user/area.csv') LIMIT 2;
-- Answer:
-- |                   GEOM                   |   ID   |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |
{% endhighlight %}

##### Separated file containing the column names on the first line
{% highlight mysql %}

CREATE TABLE AREA AS
    SELECT * FROM CSVRead('/home/user/area.csv',
                          NULL,
                          'fieldSeparator=;') LIMIT 2;
-- Answer:
-- |                    GEOM                  |   ID   |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |
{% endhighlight %}

##### Separated file with no column names on the first line

{% highlight mysql %}
CREATE TABLE AREA AS
    SELECT * FROM CSVRead('/home/user/area.csv',
                          'COLUMN1; COLUMN2',
                          'fieldSeparator=;') LIMIT 2;
-- Answer:
-- |                  COLUMN1                 | COLUMN2 |
-- | ---------------------------------------- | ------- |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |       1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |       2 |
{% endhighlight %}


##### Import a csv file (here `centroid.csv`) and create a geometric table using coordinates columns (here `coord_x` and `coord_y`)

{% highlight mysql %}
-- centroid.csv
| id | coord_x | coord_y |
|----|---------|---------|
| 1  |    2    |    3    |
| 2  |    4    |    5    |
| 3  |    6    |    7    |

CREATE TABLE POINTS(ID INT PRIMARY KEY,
                    GEOM GEOMETRY) AS
        SELECT ST_MakePoint(coord_x, coord_y) GEOM, id
        FROM CSVREAD('/home/user/centroid.csv');

SELECT * FROM POINTS;
-- Answer:
| ID |  GEOM  |
|----|------------|
| 1  | POINT(2 3) |
| 2  | POINT(4 5) |
| 3  | POINT(6 7) |
{% endhighlight %}



##### See also

* [`CSVWrite`](../CSVWrite)
* H2 <a href="http://www.h2database.com/html/functions.html#csvread"
target="_blank">CSVRead</a>
