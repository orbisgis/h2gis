---
layout: docs
title: ST_SetSRID
category: h2spatial/projections
description: Return a new Geometry with a replaced spatial reference id
prev_section: h2spatial/projections
next_section: ST_Transform
permalink: /docs/dev/ST_SetSRID/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_setSRID(GEOMETRY geom, int srid);
{% endhighlight %}

### Description

Returns a new Geometry with a replaced spatial reference id `srid`. 
Warning, use `ST_Transform` if you want to change the coordinate reference system as this method does not update the coordinates.
This function can take at first argument an instance of Geometry or Envelope.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
CREATE TABLE testSrid(the_geom GEOMETRY);
INSERT INTO testSrid VALUES (
    ST_GeomFromText('POINT(15 25)',27572));
SELECT ST_SRID(ST_SETSRID(the_geom,5321)) trans,
    ST_SRID(the_geom) original FROM testSrid;
-- Answer:
--    | TRANS | ORIGINAL |
--    |-------|----------|
--    |  5321 |  27572   |
{% endhighlight %}

##### See also

* [`ST_Transform`](../ST_Transform)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/crs/ST_SetSRID.java" target="_blank">Source code</a>
