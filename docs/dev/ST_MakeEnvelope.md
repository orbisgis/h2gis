---
layout: docs
title: ST_MakeEnvelope
category: Geometry2D/geometry-creation
description: Create a rectangular Polygon
prev_section: ST_MakeEllipse
next_section: ST_MakeGrid
permalink: /docs/dev/ST_MakeEnvelope/
---

### Signatures

{% highlight mysql %}
POLYGON ST_MakeEnvelope(DOUBLE xmin, DOUBLE ymin,
                        DOUBLE xmax, DOUBLE ymax);
POLYGON ST_MakeEnvelope(DOUBLE xmin, DOUBLE ymin,
                        DOUBLE xmax, DOUBLE ymax, INT srid);
{% endhighlight %}

### Description

Creates a rectangular `POLYGON` formed `xmin`, `xmax`, `ymin` and
`ymax`. The user may specify an `srid`.

### Examples

{% highlight mysql %}
SELECT ST_MakeEnvelope(0, 0, 1, 1);
-- Answer: POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))

SELECT ST_MakeEnvelope(0, 0, 1, 1, 4326);
-- Answer: POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))

SELECT ST_MakeEnvelope(0, -1, 2, 3);
-- Answer: POLYGON((0 -1, 2 -1, 2 3, 0 3, 0 -1))

SELECT ST_SRID(ST_MakeEnvelope(0, 0, 1, 1, 4326));
-- Answer: 4326
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/create/ST_MakeEnvelope.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>
