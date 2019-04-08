---
layout: docs
title: ST_Is3D
category: geom3D/properties
is_function: true
description: Return 1 if a Geometry has at least one z-coordinate; 0 otherwise
prev_section: geom3D/properties
next_section: ST_Z
permalink: /docs/1.5.0/ST_Is3D/
---

### Signature

{% highlight mysql %}
INT ST_Is3D(GEOMETRY geom);
{% endhighlight %}

### Description

Returns 1 if a `geom` has at least one z-coordinate; 0 otherwise.

### Examples

{% highlight mysql %}
-- No z-coordinates
SELECT ST_Is3D('LINESTRING(1 1, 2 1, 2 2, 1 2, 1 1)'::GEOMETRY);
-- Answer: 0

-- One z-coordinate
SELECT ST_Is3D('LINESTRING(1 1 1, 2 1, 2 2, 1 2, 1 1)'::GEOMETRY);
-- Answer: 1

-- All z-coordinates
SELECT ST_Is3D('LINESTRING(1 1 1, 2 1 2, 2 2 3,
                           1 2 4, 1 1 5)'::GEOMETRY);
-- Answer: 1
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_Is3D.java" target="_blank">Source code</a>
