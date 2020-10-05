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
SHPWrite(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTable);
SHPWrite(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
SHPWrite(VARCHAR path, VARCHAR tableName, 
         VARCHAR fileEncoding, BOOLEAN deleteTable);
{% endhighlight %}

### Description

Writes the content of table `tableName` into a [shapefile][wiki]
located at `path`.

`tablename` can be either:

* the name of an existing table,
* the result of a query (`SELECT` instruction which has to be written between simple quote and parenthesis `'( )'`). **Warning**: when using text value in the `WHERE` condition, you have to double the simple quote (different from double quote ""): `... WHERE TextColumn = ''myText''`.

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).


If the `deleteTable` parameter is `true` and if the specified `shapefile` already exists at the `path` address, then the `shapefile` will be removed / replaced by the new one. Else (no `deleteTable` parameter or `deleteTable` equal to `false`), an error indicating that the `shapefile` already exists will be throwned.

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

In the following example, we are working with a table named `AREA` and defined as follow.

{% highlight mysql %}
CREATE TABLE AREA(THE_GEOM GEOMETRY(POLYGON), ID INT);
INSERT INTO AREA VALUES
    ('POLYGON((-10 109, 90 9, -10 9, -10 109))', 1),
    ('POLYGON((90 109, 190 9, 90 9, 90 109))', 2);
{% endhighlight %}


#### 1. Case with `path` and `tablename`

{% highlight mysql %}
CALL SHPWrite('/home/user/area.shp', 'AREA');

-- Read it back, showing that the driver wrote POLYGONs as
-- MULTIPOLYGONs to be compatible with SHP.
CALL SHPRead('/home/user/area.shp', 'AREA2');
SELECT * FROM AREA2;
-- Answer:
-- |                     THE_GEOM                     | ID |
-- | ------------------------------------------------ | -- |
-- | MULTIPOLYGON(((-10 109,, 90 9, -10 9, -10 109))) |  1 |
-- | MULTIPOLYGON(((90 109, 190 109, 90 9, 90 109)))  |  2 |
{% endhighlight %}

#### 2. Case where `tablename` is the result of a selection

{% highlight mysql %}
CALL SHPWRITE('/home/user/area.shp', 
              '(SELECT * FROM AREA WHERE ID<2 )');

-- Read it back
CALL SHPRead('/home/user/area.shp', 'AREA2');
SELECT * FROM AREA2;
-- Answer:
-- |                     THE_GEOM                     | ID |
-- | ------------------------------------------------ | -- |
-- | MULTIPOLYGON(((-10 109,, 90 9, -10 9, -10 109))) |  1 |
{% endhighlight %}

#### Case with `deleteTable`

Export the table `AREA` into the `area.shp` file.

{% highlight mysql %}
CALL SHPWRITE('/home/user/area.shp', 'AREA');
{% endhighlight %}

The `area.shp` is created.

Now write once again, using the `deleteTable` = `true`

{% highlight mysql %}
CALL SHPWRITE('/home/user/area.shp', 'AREA', true);
{% endhighlight %}

The existing `area.shp` file is removed and replaced by the new one. 

Now, same but with `deleteTable` = `false`

{% highlight mysql %}
CALL SHPWRITE('/home/user/area.shp', 'AREA', false);
{% endhighlight %}

A message is thrown: `The file already exist`. 

### Export the .prj file

If you want to export your shapefile with it's projection, stored in a .prj file, you must assume that the table contains a SRID constraint value  greater than 0. 

If not, the SRID must be enforced using the following commands:

{% highlight mysql %}
UPDATE mytable SET the_geom = ST_SetSRID(the_geom, EPSG_CODE);
ALTER TABLE mytable ADD CHECK ST_SRID(the_geom) = EPSG_CODE;
{% endhighlight %}

Where:

* `mytable` is the table name to update
* `the_geom` is the geometric field name
* `EPSG_CODE` is the EPSG id corresponding to your system (e.g `4326` for `WGS84` or `2154` for the french `Lambert 93`).

Then export your shapefile as seen before.

##### See also

* [`SHPRead`](../SHPRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/shp/SHPWrite.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Shapefile
