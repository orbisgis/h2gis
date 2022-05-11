---
layout: docs
title: ST_SetSRID
category: geom2D/projections
is_function: true
description: Return a copy of a Geometry with a new SRID
prev_section: geom2D/projections
next_section: ST_Transform
permalink: /docs/dev/ST_SetSRID/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_SetSRID(GEOMETRY geom, INT srid);
{% endhighlight %}

### Description

Returns a copy of `geom` with spatial reference id set to `srid`.

<div class="note warning">
  <h5>ST_SetSRID does not  to actually change the SRID of <code>geom</code>.
  For this purpose, use <a href="/docs/dev/ST_Transform">ST_Transform</a>.</h5>
</div>

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
CREATE TABLE test_srid(geom GEOMETRY);
INSERT INTO test_srid VALUES (
    ST_GeomFromText('POINT(15 25)', 27572));
SELECT ST_SRID(ST_SETSRID(geom, 5321)) trans,
    ST_SRID(geom) original FROM test_srid;
-- Answer:
--    | TRANS | ORIGINAL |
--    |-------|----------|
--    |  5321 |  27572   |
{% endhighlight %}

##### See also

* [`ST_Transform`](../ST_Transform), [`ST_SRID`](../ST_SRID)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/crs/ST_SetSRID.java" target="_blank">Source code</a>
