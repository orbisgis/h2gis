---
layout: docs
title: ST_Covers
category: geom2D/predicates
is_function: true
description: Return true if no point in Geometry B is outside Geometry A
prev_section: ST_Contains
next_section: ST_Crosses
permalink: /docs/dev/ST_Covers/
---

### Signature

{% highlight mysql %}
BOOLEAN ST_Covers(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns true if no point in `geomB` is outside `geomA`.

{% include spatial_indice_warning.html %}

### Examples

| smallc POLYGON                     | bigc POLYGON                         |
|------------------------------------|--------------------------------------|
| POLYGON((1 1, 5 1, 5 4, 1 4, 1 1)) | POLYGON((0 0, 10 0, 10 5, 0 5, 0 0)) |

<img class="displayed" src="../ST_Covers.png"/>

{% highlight mysql %}
SELECT ST_Covers(smallc, smallc) FROM input_table;
-- Answer:    TRUE

SELECT ST_Covers(smallc, bigc) FROM input_table;
-- Answer:    FALSE

SELECT ST_Covers(bigc, smallc) FROM input_table;
-- Answer:    TRUE

SELECT ST_Covers(bigc, ST_ExteriorRing(bigc)) FROM input_table;
-- Answer:    TRUE

SELECT ST_Contains(bigc, ST_ExteriorRing(bigc)) FROM input_table;
-- Answer:    FALSE
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/predicates/ST_Covers.java" target="_blank">Source code</a>
