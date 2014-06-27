---
layout: docs
title: GeoJsonRead
category: h2drivers
is_function: true
description: Read a GeoJSON 1.0 file
prev_section: GPXRead
next_section: GeoJsonWrite
permalink: /docs/dev/GeoJsonRead/
---

### Signature

{% highlight mysql %}
GeoJsonRead(varchar fileName, varchar tableReference);
{% endhighlight %}

### Description
Reads a GeoJSON 1.0 file and creates the corresponding spatial table.

### Examples

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data/file.GeoJson', 
             'database.schema.tableName');

CALL GeoJsonRead('/home/user/Data/multi.geojson',
                 'table_polygon');
select * from table_polygon;
-- Answer: 
-- |               THE_GEOM               | IDAREA | CLIMAT |
-- | ------------------------------------ | ------ | ------ |
-- | POLYGON((120 370, 180 370, 120 370)) |      1 | bad    |
{% endhighlight %}

##### See also

* [`GeoJsonWrite`](../GeoJsonWrite)
* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/geojson/GeoJsonRead.java" target="_blank">Source code</a>
