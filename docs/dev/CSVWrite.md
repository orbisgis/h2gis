---
layout: docs
title: CSVWrite
category: h2drivers
is_function: true
description: Table &rarr; CSV
prev_section: CSVRead
next_section: DBFRead
permalink: /docs/dev/CSVWrite/
---

### Signatures

{% highlight mysql %}
CSVWrite(VARCHAR path, VARCHAR sqlSelectTable);
CSVWrite(VARCHAR path, VARCHAR sqlSelectTable,
         VARCHAR stringDecode);
{% endhighlight %}

### Description

<div class="note">
  <h5>This function is a part of H2.</h5>
  <p>Please first consult its
  <a href="http://www.h2database.com/html/functions.html#csvwrite"
  target="_blank">documentation</a> on the H2 website.</p>
</div>

Writes a CSV file from the SQL select statement `sqlSelectTable` to
the CSV file specified by `path`.

{% include stringDecode.html %}

### Examples

{% highlight mysql %}
-- Create an example table to use with CSVWrite:
CREATE TABLE AREA(GEOM VARCHAR(100), ID INT PRIMARY KEY);
INSERT INTO AREA VALUES
    ('POLYGON((-10 109, 90 9, -10 9, -10 109))', 1),
    ('POLYGON((90 109, 190 9, 90 9, 90 109))', 2);
-- Write it to a CSV file:
CALL CSVWrite('/home/user/area.csv', 'SELECT * FROM AREA');
-- Read it back:
SELECT * FROM CSVRead('/home/user/area.csv');
-- Answer:
-- |                   GEOM                   |   ID   |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |

-- Try writing it with a specific charset and field separator:
CALL CSVWRITE('/home/user/area.csv',
              'SELECT * FROM AREA', 'charset=UTF-8
                                     fieldSeparator=;');
-- Read it back:
SELECT * FROM CSVRead('/home/user/area.csv',
                      NULL,
                      'charset=UTF-8 fieldSeparator=;');
-- Answer:
-- |                       GEOM               |   ID   |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |
{% endhighlight %}

##### See also

* [`CSVRead`](../CSVRead)
* H2 <a href="http://www.h2database.com/html/functions.html#csvwrite"
target="_blank">CSVWrite</a>
