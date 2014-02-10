---
layout: docs
title: ST_SRID
category: h2spatial/properties
description: 
prev_section: ST_PointOnSurface
next_section: ST_StartPoint
permalink: /docs/dev/ST_SRID/
---

### Signatures

{% highlight mysql %}
int ST_SRID(binary geom);
{% endhighlight %}

### Description

Retrieves the SRID from an EWKB encoded geometry. Returns SRID value or 0 if input geometry does not have one.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_SRID(ST_Envelope('LINESTRING(1 1,5 5)', 27572));
-- Answer: 27572

CREATE TABLE testSrid(the_geom GEOMETRY);
INSERT INTO testSrid values (ST_GeomFromText('POINT(15 25)', 2154));
SELECT ST_SRID(the_geom) FROM testSrid;
-- Answer: 2154
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_SRID.java" target="_blank">Source code</a>
