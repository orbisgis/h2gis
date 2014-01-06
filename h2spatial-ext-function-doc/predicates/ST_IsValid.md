---

layout: docs

title: ST_IsValid

prev_section: dev/ST_IsRectangle

next_section: dev/properties

permalink: /docs/dev/ST_IsValid/

---

### Signature

{% highlight mysql %}
boolean ST_IsValid(Geometry geom);
{% endhighlight %}

### Description

Returns true if `geom` is valid.

### Examples

{% highlight mysql %}
SELECT ST_IsValid('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'::Geometry);
-- Answer:    true

SELECT ST_IsValid('POLYGON ((0 0, 10 0, 10 5, 10 -5, 0 0))'::Geometry);
-- Answer:    false
{% endhighlight %}

##### History

* Added: [#26](https://github.com/irstv/H2GIS/pull/26)
