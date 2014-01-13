---
layout: docs
title: ST_Covers
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

| smallc Polygon | bigc Polygon |
| ----|---- |
| ST_Buffer(ST_GeomFromText( 'POINT(1 2)'), 10) | ST_Buffer(ST_GeomFromText( 'POINT(1 2)'), 20)) |

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

* [Source code](https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/predicates/ST_Covers.java)
* Added: [#26](https://github.com/irstv/H2GIS/pull/26)
