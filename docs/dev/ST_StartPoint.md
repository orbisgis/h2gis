---
layout: docs
title: ST_StartPoint
category: h2spatial/properties
description: Return the first point of a <code>LINESTRING</code>
prev_section: ST_SRID
next_section: ST_X
permalink: /docs/dev/ST_StartPoint/
---

### Signature

{% highlight mysql %}
POINT ST_StartPoint(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the first `POINT` of a `LINESTRING` as a `POINT`.
If the input parameter is not a `LINESTRING` this function returns `null`.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_StartPoint('LINESTRING(1 2, 5 3, 2 6)');
-- Answer: POINT(1 2)
{% endhighlight %}

<img class="displayed" src="../ST_StartPoint.png"/>

##### See also

* [`ST_EndPoint`](../ST_EndPoint)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_StartPoint.java" target="_blank">Source code</a>
