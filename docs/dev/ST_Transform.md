---
layout: docs
title: ST_Transform
category: h2spatial/projections
description: Transform a Geometry from one CRS to another
prev_section: ST_SetSRID
next_section: h2spatial/properties
permalink: /docs/dev/ST_Transform/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Transform(GEOMETRY geom, int SRID);
{% endhighlight %}

### Description

This function is used to transform a `GEOMETRY` from one CRS coordinate reference system to another.
Only integer codes available in the spatial_ref_sys table are allowed.

*Note:* If you want know the `SRID` of a CRS go to h2gis and type: SELECT * FROM spatial_ref_sys;
The `SRID` is principaly CodeEPSG but the spatial_ref_sys table can be enriched by other CRS.
The other CRS are not recognized by the EPSG but they have a `SRID`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
CREATE TABLE init AS SELECT 
    ST_GeomFromText('POINT(584173.736059813 2594514.82833411)',
    27572) As the_geom;
SELECT ST_TRANSFORM(the_geom, 4326) FROM init;
-- Answer: POINT (2.114551398096724 50.34560979151726)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/crs/ST_Transform.java" target="_blank">Source code</a>
