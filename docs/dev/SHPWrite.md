---
layout: docs
title: SHPWrite
category: h2drivers
is_function: true
description: Table &rarr; SHP
prev_section: SHPRead
next_section: ST_AsGeoJson
permalink: /docs/dev/SHPWrite/
---

### Signatures

{% highlight mysql %}
SHPWrite(VARCHAR path, VARCHAR tableName);
SHPWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
{% endhighlight %}

### Description

Writes the contents of table `tableName` to a [shapefile][wiki]
located at `path`.
The default value of `fileEncoding` is `ISO-8859-1`.

<div class="note warning">
  <h5>Shapefiles do not support arbitrary geometrical data.</h5>
  <p>They do not support:
  <ul>
    <li><code>POLYGON</code>s (they are automatically converted to
        <code>MULTIPOLYGON</code>s when exported)</li>
    <li><code>NULL</code> Geometries</li>
    <li>Multiple Geometry types in the same table</li>
  </ul></p>
</div>

### Examples

{% highlight mysql %}
-- Create an example table containing POLYGONs and export it.
CREATE TABLE AREA(THE_GEOM GEOMETRY, ID INT PRIMARY KEY);
INSERT INTO AREA VALUES
    ('POLYGON((-10 109, 90 9, -10 9, -10 109))', 1),
    ('POLYGON((90 109, 190 9, 90 9, 90 109))', 2);
CALL SHPWrite('/home/user/area.shp', 'AREA');

-- Read it back, showing that the driver wrote POLYGONs as
-- MULTIPOLYGONs to be compatible with SHP.
CALL SHPRead('/home/user/area.shp', 'AREA2');
SELECT * FROM AREA2;
-- Answer:
-- |                     THE_GEOM                     | IDAREA |
-- | ------------------------------------------------ | ------ |
-- | MULTIPOLYGON(((-10 109,, 90 9, -10 9, -10 109))) |      1 |
-- | MULTIPOLYGON(((90 109, 190 109, 90 9, 90 109)))  |      2 |
{% endhighlight %}


### Export with the .prj file

If you want to export your shapefile with it's projection (.prj) file, you have first to force the SRID associated to the geometry. To do so, just execute the two following instructions.

{% highlight mysql %}
UPDATE mytable SET the_geom = ST_SetSRID(the_geom, EPSG_CODE);
ALTER TABLE mytable ADD CHECK ST_SRID(the_geom) = EPSG_CODE;
{% endhighlight %}

Where:

* `mytable` is the table name to update
* `the_geom` is the geometric field name
* `EPSG_CODE` is the EPSG id corresponding to your system (e.g `4326` for `WGS84` or `2154` for the french `Lambert 93`).

Once done, just export your shapefile as seen before.

##### See also

* [`SHPRead`](../SHPRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/shp/SHPWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Shapefile
