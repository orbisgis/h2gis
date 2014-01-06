---

layout: docs

title: ST_YMax

prev_section: dev/ST_XMin

next_section: dev/ST_YMin

permalink: /docs/dev/ST_YMax/

---

### Signature

{% highlight mysql %}
POINT ST_YMax(Geometry geom);
{% endhighlight %}

### Description

Returns the maximum y-value of the given geometry.

### Example

{% highlight mysql %}
SELECT ST_YMax('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    5.0
{% endhighlight %}

![warning](../images/illustations/properties/ST_YMax.png)

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
