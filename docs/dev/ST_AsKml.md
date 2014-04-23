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

For KML profil : CLAMPTOGROUND = 1; RELATIVETOGROUND = 2; ABSOLUTE = 4; 
For GX profil : CLAMPTOSEAFLOOR = 8; RELATIVETOSEAFLOOR = 16; No altitude : NONE = 0;

### Examples

{% highlight mysql %}
CREATE TABLE kml_point(id int primary key, the_geom POINT, response boolean);
INSERT INTO kml_point values(1, ST_Geomfromtext('POINT (2.19 47.58)',4326), true);
SELECT ST_AsKml(the_geom) FROM kml_point;
-- Answer: <Point><coordinates>2.19,47.58</coordinates></Point>

{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/kml/ST_AsKml.java" target="_blank">Source code</a>
