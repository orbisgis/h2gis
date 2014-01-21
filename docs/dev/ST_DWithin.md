---
layout: docs
title: ST_DWithin
category: h2spatial-ext/predicates
description: Return true if the geometries are within the specified distance of one another
prev_section: ST_Covers
next_section: ST_IsRectangle
permalink: /docs/dev/ST_DWithin/
---

### Signature

{% highlight mysql %}
boolean ST_DWithin(Geometry geomA, Geometry geomB, double distance);
{% endhighlight %}

### Description

Returns true if `geomA` is within `distance` of `geomB`.

### Examples


| geomA Polygon | geomB Polygon |
| ----|---- |
| POLYGON((0 0, 10 0, 10 5, 0 5, 0 0)) | POLYGON((12 0, 14 0, 14 6, 12 6, 12 0)) |

<img class="displayed" src="../ST_DWithin.png"/>

{% highlight mysql %}
SELECT ST_DWithin(geomA, geomB, 2.0) FROM input_table;
-- Answer:    true

SELECT ST_DWithin(geomA, geomB, 1.0) FROM input_table;
-- Answer:    false

SELECT ST_DWithin(geomA, geomB, -1.0) FROM input_table;
-- Answer:    false

SELECT ST_DWithin(geomA, geomB, 3.0) FROM input_table;
-- Answer:    true

SELECT ST_DWithin(geomA, geomA, -1.0) FROM input_table;
-- Answer:    false

SELECT ST_DWithin(geomA, geomA, 0.0) FROM input_table;
-- Answer:    true

SELECT ST_DWithin(geomA, geomA, 5000.0) FROM input_table;
-- Answer:    true
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/predicates/ST_DWithin.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/26" target="_blank">#26</a>
