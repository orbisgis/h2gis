---
layout: docs
title: ST_IsEmpty
category: h2spatial/properties
description: Return true if a <code>GEOMETRY</code> is empty
prev_section: ST_IsClosed
next_section: ST_IsRing
permalink: /docs/dev/ST_IsEmpty/
---

### Signature

{% highlight mysql %}
boolean ST_IsEmpty(GEOMETRY geom);
{% endhighlight %}

### Description

Returns true if a Geometry is empty.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_IsEmpty('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: FALSE

SELECT ST_IsEmpty('GEOMETRYCOLLECTION(
                      MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
                      LINESTRING(2 6, 6 2), 
                      POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: FALSE

SELECT ST_IsEmpty('POLYGON EMPTY');
-- Answer: TRUE
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_IsEmpty.java" target="_blank">Source code</a>
