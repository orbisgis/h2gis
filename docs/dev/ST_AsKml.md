---
layout: docs
title: ST_AsKml
category: h2drivers/KML
description: Convert Geometry to a KML Geometry representation
prev_section: KMLWrite
next_section: h2drivers/SHP
permalink: /docs/dev/ST_AsKml/
---

### Signatures

{% highlight mysql %}
varchar ST_AsKml(GEOMETRY geom);
varchar ST_AsKml(GEOMETRY geom, boolean extrude, int altitudeModeEnum);
{% endhighlight %}

### Description
Returns the Geometry as a Keyhole Markup Language (KML) element.  
Available extrude values are true, false or none. Supported altitude mode : 

For KML profil : clampToGround = 1; relativeToGround = 2; absolute = 4; 

For GX profil : clampToSeaFloor = 8; relativeToSeaFloor = 16; 

No altitude : NONE = 0;

### Examples

|               Geometry POINT              |     |
| ----------------------------------------- | --- |
| ST_Geomfromtext('POINT(2.19 47.58),4326') |     |

{% highlight mysql %}
SELECT ST_AsKml(the_geom) FROM input_table;
-- Answer: <Point><coordinates>2.19,47.58</coordinates></Point>

SELECT ST_AsKml(the_geom, true, 1) FROM input_table;
-- Answer: <Point><extrude>1</extrude>
--    <kml:altitudeMode>clampToGround</kml:altitudeMode>
--    <coordinates>2.19,47.58</coordinates>
--         </Point>

SELECT ST_AsKml(the_geom, false, 16) FROM input_table;
-- Answer: <Point><extrude>0</extrude>
--    <kml:altitudeMode>relativeToSeaFloor</kml:altitudeMode>
--    <coordinates>2.19,47.58</coordinates>
--         </Point>

CREATE TABLE kml_line(id int primary key, the_geom LINESTRING, 
                       response boolean);
INSERT INTO kml_line values(1, ST_Geomfromtext(
    'LINESTRING(-1.53 47.24 100, -1.51 47.22 100, -1.50 47.19 100, 
                -1.49 47.17 100)',4326), true);
SELECT ST_AsKml(the_geom, true, 2) FROM kml_line;
-- Answer: <LineString><extrude>1</extrude>
--    <kml:altitudeMode>relativeToGround</kml:altitudeMode>
--    <coordinates>-1.53,47.24,100.0 -1.51,47.22,100.0 
--                 -1.5,47.19,100.0 -1.49,47.17,100.0</coordinates>
--         </LineString>
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/kml/ST_AsKml.java" target="_blank">Source code</a>
