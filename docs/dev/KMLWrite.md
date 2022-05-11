---
layout: docs
title: KMLWrite
category: h2drivers
is_function: true
description: KML, KMZ &rarr; Table
prev_section: JsonWrite
next_section: SHPRead
permalink: /docs/dev/KMLWrite/
---

### Signature

{% highlight mysql %}
KMLWrite(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Writes table `tableName` to a [KML][wiki] file located at `path`.
A coordinate reference system must be set to save a KML file.

### Examples

{% highlight mysql %}
-- Create an example table to write to a KML file:
CREATE TABLE TEST(ID INT PRIMARY KEY, GEOM POINT);
INSERT INTO TEST
    VALUES (1, ST_GeomFromText('POINT(2.19 47.58)', 4326));

-- Write it:
CALL KMLWrite('/home/user/test.kml', 'TEST');
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/kml/KMLWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Keyhole_Markup_Language
