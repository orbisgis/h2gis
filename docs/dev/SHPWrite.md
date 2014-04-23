---
layout: docs
title: SHPWrite
category: h2drivers/SHP
description: Write shape files
prev_section: SHPRead
next_section: Geometry2D/list-function-2d
permalink: /docs/dev/SHPWrite/
---

### Signatures

{% highlight mysql %}
SHPWrite(varchar fileName, varchar tableReference);
SHPWrite(varchar fileName, varchar tableReference, 
         varchar fileEncoding);
{% endhighlight %}

### Description
Transfers the content of a table into a new shape file.
The driver cannot create Polygon, it creates Multipolygon.
Shape files don't support null Geometry values.
Shape files don't support to have different Geometry type in the same table.

### Examples

{% highlight mysql %}
create table area(the_geom GEOMETRY, idarea int primary key); 
insert into area values('POLYGON((-10 109, 90 9, -10 9, 
                                  -10 109))', 1); 
insert into area values('POLYGON((90 109, 190 9, 90 9, 
                                  90 109))', 2); 
CALL SHPWrite('/home/user/donnees_sig/Data/area_export.shp', 
              'AREA'); 
CALL SHPread('/home/user/donnees_sig/Data/area_export.shp', 
             'AREA2');
Select * from AREA2;
-- Answer: The driver can not create POLYGON, it creates 
--         MULTIPOLYGON.
-- |                     THE_GEOM                     | IDAREA |
-- | ------------------------------------------------ | ------ |
-- | MULTIPOLYGON(((-10 109,, 90 9, -10 9, -10 109))) |      1 |
-- | MULTIPOLYGON(((90 109, 190 109, 90 9, 90 109)))  |      2 |
{% endhighlight %}

##### See also

* [`SHPRead`](../SHPRead)
* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/shp/SHPWrite.java" target="_blank">Source code</a>
