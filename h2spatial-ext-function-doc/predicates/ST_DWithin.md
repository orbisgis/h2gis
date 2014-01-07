---

layout: docs

title: ST_DWithin

prev_section: dev/ST_Covers

next_section: dev/ST_IsRectangle

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

##### History

* Added: [#26](https://github.com/irstv/H2GIS/pull/26)
