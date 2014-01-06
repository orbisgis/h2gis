---

layout: docs

title: ST_XMin

prev_section: dev/ST_XMax

next_section: dev/ST_YMax

permalink: /docs/dev/ST_XMin/

---

### Signature

{% highlight mysql %}
POINT ST_XMin(Geometry geom);
{% endhighlight %}

### Description

Returns the minimum x-value of the given geometry.

### Example

{% highlight mysql %}
SELECT ST_XMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    1.0
{% endhighlight %}

![warning](../images/illustations/properties/ST_XMin.png)

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
