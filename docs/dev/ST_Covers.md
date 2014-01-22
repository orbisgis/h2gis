---
layout: docs
title: ST_Covers
category: h2spatial-ext/predicates
description: Return true if no point in geometry B is outside geometry A
prev_section: h2spatial-ext/predicates
next_section: ST_DWithin
permalink: /docs/dev/ST_Covers/
---

### Signature

{% highlight mysql %}
boolean ST_Covers(Geometry geomA, Geometry geomB);
{% endhighlight %}

### Description

Returns true if no point in `geomB` is outside `geomA`.

### Examples

| smallc POLYGON | bigc POLYGON |
| ----|---- |
| POLYGON((1 1, 5 1, 5 4, 1 4, 1 1)) | POLYGON((0 0, 10 0, 10 5, 0 5, 0 0)) |

<img class="displayed" src="../ST_Covers.png"/>

{% highlight mysql %}
SELECT ST_Covers(smallc, smallc) FROM input_table;
-- Answer:    true

SELECT ST_Covers(smallc, bigc) FROM input_table;
-- Answer:    false

SELECT ST_Covers(bigc, smallc) FROM input_table;
-- Answer:    true

SELECT ST_Covers(bigc, ST_ExteriorRing(bigc)) FROM input_table;
-- Answer:    true

SELECT ST_Contains(bigc, ST_ExteriorRing(bigc)) FROM input_table;
-- Answer:    false
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/predicates/ST_Covers.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/26" target="_blank">#26</a>
