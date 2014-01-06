---

layout: docs

title: ST_ZMax

prev_section: dev/ST_YMin

next_section: dev/ST_ZMin

permalink: /docs/dev/ST_ZMax/

---

### Signature

{% highlight mysql %}
POINT ST_ZMax(Geometry geom);
{% endhighlight %}

### Description

Returns the maximum z-value of the given geometry.

### Examples

{% highlight mysql %}
SELECT ST_ZMax('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    6.0
{% endhighlight %}

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
