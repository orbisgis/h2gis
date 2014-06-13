---
layout: docs
title: CSVWrite
category: h2drivers/CSV
description: Write a CSV file
prev_section: CSVRead
next_section: h2drivers/file_table
permalink: /docs/dev/CSVWrite/
---

### Signatures

{% highlight mysql %}
CSVWrite(varchar fileName, varchar sqlSelectTable);
CSVWrite(varchar fileName, varchar sqlSelectTable, 
         varchar stringDecode);
{% endhighlight %}

### Description
Writes a CSV file.
By default the stringDecode is :

* charset =UTF-8 
* fieldDelimiter =" 
* fieldSeparator =, 
* lineSeparator =\n
* writeColumnHeader =true

### Examples

{% highlight mysql %}
create table area(the_geom varchar(100), idarea int primary key); 
insert into area values('POLYGON((-10 109, 90 9, -10 9, 
                                  -10 109))', 1); 
insert into area values('POLYGON((90 109, 190 9, 90 9, 
                                  90 109))', 2); 

CALL CSVWRITE('/home/Documents/area.csv', 
              'SELECT * FROM area');
CREATE TABLE area2 AS SELECT * FROM CSVRead(
   '/home/Documents/area.csv');
Select * from AREA2;
-- Answer:
-- |                 the_geom                 | idarea |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |

CALL CSVWRITE('/home/Documents/area.csv', 
              'SELECT * FROM area', 'charset=UTF-8 
                                     fieldSeparator=;');
CREATE TABLE area2 AS SELECT * FROM CSVRead(
   '/home/Documents/area.csv', null, 
   'charset=UTF-8 fieldSeparator=;');
Select * from AREA2;
-- Answer: 
-- |                     THE_GEOM             | IDAREA |
-- | ---------------------------------------- | ------ |
-- | POLYGON((-10 109, 90 9, -10 9, -10 109)) |      1 |
-- | POLYGON((90 109, 190 9, 90 9,  90 109))  |      2 |


{% endhighlight %}

##### See also

* [`CSVRead`](../CSVRead)
* <a href="http://www.h2database.com/html/grammar.html#csv_options"
target="_blank">H2 grammar</a>
