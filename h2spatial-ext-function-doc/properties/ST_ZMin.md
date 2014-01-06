---

layout: docs

title: ST_ZMin

prev_section: dev/ST_ZMax

next_section:

permalink: /docs/dev/ST_ZMin/

---

### Signature

{% highlight mysql %}
POINT ST_ZMin(Geometry geom);
{% endhighlight %}

### Description

Returns the minimum z-value of the given geometry.

### Examples

{% highlight mysql %}
SELECT ST_ZMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    3.0
{% endhighlight %}

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
