---
layout: docs
title: GeoJsonWrite
category: h2drivers
is_function: true
description: Write a GeoJSON 1.0 file
prev_section: GeoJsonRead
next_section: KMLWrite
permalink: /docs/dev/GeoJsonWrite/
---

### Signature

{% highlight mysql %}
GeoJsonWrite(varchar fileName, varchar tableReference);
{% endhighlight %}

### Description
Transfers the content of a spatial table to a GeoJSON 1.0 file.

### Examples

{% highlight mysql %}
CREATE TABLE table_multipolygon(idarea int primary key, 
                                the_geom MULTIPOLYGON);
INSERT INTO table_multipolygon values(1, 
    'MULTIPOLYGON(((120 370, 180 370, 120 370)),  
                  ((162 245, 234 245, 234 175, 162 245)))');
CALL GeoJsonWrite('/home/user/Data/area.geojson', 
                  'table_multipolygon');

CALL GeoJsonRead('/home/user/Data/area.geojson',
                 'table_area');
select * from table_area;
-- Answer: 
-- |                  THE_GEOM                 | IDAREA |
-- | ----------------------------------------- | ------ |
-- | MULTIPOLYGON((120 370, 180 370, 120 370)) |      1 |
-- |   ((162 245, 234 245, 234 175, 162 245))) |        |

{% endhighlight %}

##### See also

* [`GeoJsonRead`](../GeoJsonRead)
* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/geojson/GeoJsonWrite.java" target="_blank">Source code</a>
