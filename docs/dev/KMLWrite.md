---
layout: docs
title: KMLWrite
category: h2drivers/KML
description: Write KML or KMZ files
prev_section: h2drivers/KML
next_section: ST_AsKml
permalink: /docs/dev/KMLWrite/
---

### Signature

{% highlight mysql %}
KMLWrite(varchar fileName, varchar tableReference);
{% endhighlight %}

### Description
Transfers the content of a spatial table to a KML or KMZ file.

### Examples

{% highlight mysql %}
CALL KMLWrite('/home/user/Data/kml_points.kml',
              'database.schema.tableName');

CREATE TABLE KML_POINTS(id int primary key, the_geom POINT, 
                        response boolean);
INSERT INTO KML_POINTS values(1, ST_Geomfromtext(
                                  'POINT(2.19 47.58)', 4326), 
                              true);
INSERT INTO KML_POINTS values(2, ST_Geomfromtext(
                                  'POINT(1.06 47.59)', 4326), 
                              false);

CALL KMLWrite('/home/user/Data/kml_points.kml', 'KML_POINTS');
CALL KMLWrite('/home/user/Data/kml_points.kmz', 'KML_POINTS');
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/kml/KMLWrite.java" target="_blank">Source code</a>
