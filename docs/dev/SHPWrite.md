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

`tablename` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`).


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
CREATE TABLE AREA(GEOM GEOMETRY, ID INT PRIMARY KEY);
INSERT INTO AREA VALUES
    ('POLYGON((-10 109, 90 9, -10 9, -10 109))', 1),
    ('POLYGON((90 109, 190 9, 90 9, 90 109))', 2);
CALL SHPWrite('/home/user/area.shp', 'AREA');

-- Read it back, showing that the driver wrote POLYGONs as
-- MULTIPOLYGONs to be compatible with SHP.
CALL SHPRead('/home/user/area.shp', 'AREA2');
SELECT * FROM AREA2;
-- Answer:
-- |                       GEOM                       | ID |
-- | ------------------------------------------------ | -- |
-- | MULTIPOLYGON(((-10 109,, 90 9, -10 9, -10 109))) |  1 |
-- | MULTIPOLYGON(((90 109, 190 109, 90 9, 90 109)))  |  2 |
{% endhighlight %}

#### Case where `tablename` is the result of a selection.

{% highlight mysql %}
CALL SHPWRITE('/home/user/area.shp', 
              '(SELECT * FROM AREA WHERE ID<2 )');

-- Read it back
CALL SHPRead('/home/user/area.shp', 'AREA2');
SELECT * FROM AREA2;
-- Answer:
-- |                       GEOM                       | ID |
-- | ------------------------------------------------ | -- |
-- | MULTIPOLYGON(((-10 109,, 90 9, -10 9, -10 109))) |  1 |
{% endhighlight %}

### Export the .prj file

If you want to export your shapefile with it's projection, stored in a .prj file, you must assume that the table contains a SRID constraint value  greater than 0. 

If not, the SRID must be enforced using the following commands:

{% highlight mysql %}
UPDATE mytable SET geom = ST_SetSRID(geom, EPSG_CODE);
ALTER TABLE mytable ADD CHECK ST_SRID(geom) = EPSG_CODE;
{% endhighlight %}

Where:

* `mytable` is the table name to update
* `geom` is the geometric field name
* `EPSG_CODE` is the EPSG id corresponding to your system (e.g `4326` for `WGS84` or `2154` for the french `Lambert 93`).

Then export your shapefile as seen before.

##### See also

* [`SHPRead`](../SHPRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/shp/SHPWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Shapefile
