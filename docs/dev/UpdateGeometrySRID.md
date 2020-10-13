---
layout: docs
title: UpdateGeometrySRID
category: geom2D/projections
is_function: true
description: Update the SRID of a geometry column
prev_section: ST_Transform
next_section: geom2D/properties
permalink: /docs/dev/UpdateGeometrySRID/
---

### Signatures

{% highlight mysql %}
BOOLEAN UpdateGeometrySRID(VARCHAR tableName, 
                           GEOMETRY geom, 
                           INT srid);
{% endhighlight %}

### Description

Updates the `srid` of all features in a geometry column (`geom`) from a table (`tableName`).

Returns `TRUE` if the `srid` has been updated.

<div class="note warning">
  <h5><code>UpdateGeometrySRID</code> does not to actually change the projection of <code>geom</code>.
  For this purpose, use <a href="/docs/dev/ST_Transform">ST_Transform</a>.</h5>
</div>


### Examples

{% highlight mysql %}
-- Create a table and insert a POINT with a SRID equal to 0
CREATE TABLE GEO_POINT (THE_GEOM GEOMETRY(POINT));
INSERT INTO GEO_POINT VALUES('SRID=0;POINT(0 0)');

-- Check the SRID
SELECT ST_SRID(THE_GEOM) as SRID FROM GEO_POINT;
-- Answer: SRID = 0

---------------------------
-- Update the SRID
SELECT UpdateGeometrySRID('GEO_POINT','THE_GEOM',4326);

-- And check the SRID
SELECT ST_SRID(THE_GEOM) as SRID FROM GEO_POINT;
-- Answer: SRID = 4326
{% endhighlight %}


##### See also

* [`ST_Transform`](../ST_Transform), [`ST_SRID`](../ST_SRID), [`ST_SetSRID`](../ST_SetSRID)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/crs/UpdateGeometrySRID.java" target="_blank">Source code</a>
