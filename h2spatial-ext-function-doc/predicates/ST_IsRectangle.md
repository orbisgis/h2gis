---

layout: docs

title: ST_IsRectangle

prev_section: dev/ST_DWithin

next_section: dev/ST_IsValid

permalink: /docs/dev/ST_IsRectangle/

---

### Signature

{% highlight mysql %}
boolean ST_IsRectangle(Geometry geom);
{% endhighlight %}

### Description

Returns true if `geom` is a rectangle.

### Examples

{% highlight mysql %}
SELECT ST_IsRectangle('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))'::Geometry);
-- Answer:    true

SELECT ST_IsRectangle('POLYGON ((0 0, 10 0, 10 7, 0 5, 0 0))'::Geometry);
-- Answer:    false
{% endhighlight %}

##### History

* Added: [#26](https://github.com/irstv/H2GIS/pull/26)
