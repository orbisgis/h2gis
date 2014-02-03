---
layout: docs
title: ST_Dimension
category: h2spatial/properties
description: Return dimension of a Geometry
prev_section: ST_Centroid
next_section: ST_Distance
permalink: /docs/dev/ST_Dimension/
---

### Signature

{% highlight mysql %}
integer ST_Dimension(GEOMETRY geom);
{% endhighlight %}

### Description

Return dimension of a Geometry. 0 for a `(MULTI)POINT`, 1 for a `(MULTI)LINESTRING` and 2 for a `(MULTI)POLYGON` or `GEOMETRYCOLLECTION`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_dimension('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: 0

SELECT ST_dimension('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 1

SELECT ST_dimension('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)), 
                     ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: 2

SELECT ST_dimension('GEOMETRYCOLLECTION(
                        MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
                        LINESTRING(2 6, 6 2), 
                        POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 2
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Dimension.java" target="_blank">Source code</a>
