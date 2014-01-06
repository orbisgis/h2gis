---

layout: docs

title: ST_YMin

prev_section: dev/ST_YMax

next_section: dev/ST_ZMax

permalink: /docs/dev/ST_YMin/

---

### Signature

{% highlight mysql %}
POINT ST_YMin(Geometry geom);
{% endhighlight %}

### Description

Returns the minimum y-value of the given geometry.

### Examples

{% highlight mysql %}
SELECT ST_YMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    2.0
{% endhighlight %}

![warning](../images/illustations/properties/ST_YMin.png)

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
