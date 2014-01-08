---

layout: docs

title: ST_MakePoint

prev_section: h2spatial-ext/geometry-creation

next_section:

permalink: /docs/dev/h2spatial-ext/ST_MakePoint/

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

##### See also

* [Source code](https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/create/ST_MakePoint.java)
* Added: [#69](https://github.com/irstv/H2GIS/pull/69)
