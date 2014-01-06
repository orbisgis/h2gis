---

layout: docs

title: ST_MakePoint

prev_section: dev/geometry-creation

next_section:

permalink: /docs/dev/ST_MakePoint/

---

### Signatures

{% highlight mysql %}
POINT ST_MakePoint(double x, double y);
POINT ST_MakePoint(double x, double y, double z);
{% endhighlight %}

### Description

Constructs a `POINT` from `x` and `y` (and possibly `z`).

### Examples

{% highlight mysql %}
SELECT ST_MakePoint(1.4, -3.7);
-- Answer:     POINT(1.4 -3.7)

SELECT ST_MakePoint(1.4, -3.7, 6.2);
-- Answer:     POINT(1.4 -3.7 6.2)
{% endhighlight %}

##### History

* Added: [#69](https://github.com/irstv/H2GIS/pull/69)
