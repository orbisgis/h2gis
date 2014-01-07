---

layout: docs

title: ST_XMax

prev_section: dev/ST_Extent

next_section: dev/ST_XMin

permalink: /docs/dev/ST_XMax/

---

### Signature

{% highlight mysql %}
POINT ST_XMax(Geometry geom);
{% endhighlight %}

### Description

Returns the maximum x-value of the given geometry.

### Example

{% highlight mysql %}
SELECT ST_XMax('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    4.0
{% endhighlight %}

![warning](../images/illustations/properties/ST_XMax.png)

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* [Source code](https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_XMax.java)
* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
