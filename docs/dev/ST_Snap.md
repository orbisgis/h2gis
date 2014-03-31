---
layout: docs
title: ST_Snap
category: h2spatial-ext/process-geometries
description: 
prev_section: ST_SimplifyPreserveTopology
next_section: ST_Split
permalink: /docs/dev/ST_Snap/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_Snap(GEOMETRY geomA, GEOMETRY geomB, double distance);
{% endhighlight %}

### Description
Snaps two geometries together with a given tolerance.

### Examples

{% highlight mysql %}
SELECT ST_Snap('LINESTRING(1 2, 2 4, 4 4, 5 2)', 
               'LINESTRING(5 2, 2 1, 1 2)',
               1);
-- Answer: LINESTRING(1 2, 2 4, 4 4, 5 2)

SELECT ST_Snap('LINESTRING(1 2, 2 4, 4 4, 5 2)', 
               'LINESTRING(5 2, 2 1, 1 2)',
               2);
-- Answer: LINESTRING(1 2, 2 1, 2 4, 4 4, 5 2)

SELECT ST_Snap('LINESTRING(1 2, 2 4, 4 4, 5 2)', 
               'LINESTRING(5 2, 2 1, 1 2)',
               3);
-- Answer:LINESTRING (1 2, 1 2, 2 1, 5 2, 5 2)
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/processing/ST_Snap.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/80" target="_blank">#80</a>

