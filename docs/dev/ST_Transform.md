---
layout: docs
title: ST_Transform
category: geom2D/projections
is_function: true
description: Transform a Geometry from one CRS to another
prev_section: ST_SetSRID
next_section: UpdateGeometrySRID
permalink: /docs/dev/ST_Transform/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Transform(GEOMETRY geom, INT srid);
{% endhighlight %}

### Description

Transforms `geom` from its original coordinate reference system (CRS) to the
CRS specified by `srid`.

<div class="note">
    <h5>Find the SRID you're looking for.</h5>
    <p> All available CRS SRIDs may be found by executing
    <code> SELECT * FROM SPATIAL_REF_SYS; </code>
    Most SRIDs are EPSG, but the <code>SPATIAL_REF_SYS</code> table may be
    enriched by other CRSes.</p>
</div>

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Transform(ST_GeomFromText(
    'POINT(584173 2594514)', 27572), 4326);
-- Answer: POINT(2.1145411092971056 50.345602339855326)
{% endhighlight %}

##### See also



* [`ST_SetSRID`](../ST_SetSRID), [`UpdateGeometrySRID`](../UpdateGeometrySRID), [`ST_SRID`](../ST_SRID)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/crs/ST_Transform.java" target="_blank">Source code</a>
