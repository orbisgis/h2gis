---
layout: docs
title: ST_EndPoint
category: h2spatial/properties
description: Return the last point of a <code>LINESTRING</code>
prev_section: ST_Distance
next_section: ST_Envelope
permalink: /docs/dev/ST_EndPoint/
---

### Signature

{% highlight mysql %}
POINT ST_EndPoint(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the last `POINT` of a `LINESTRING` as a `POINT`.
If the input parameter is not a `LINESTRING` this function returns `null`.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_EndPoint('LINESTRING(1 2, 5 3, 2 6)');
-- Answer: POINT(2 6)
{% endhighlight %}

<img class="displayed" src="../ST_EndPoint.png"/>

{% highlight mysql %}
SELECT ST_EndPoint('MULTILINESTRING((1 1, 3 2, 3 1),
                    (1 2, 5 3, 2 6))');
-- Answer: null
{% endhighlight %}

##### See also

* [`ST_StartPoint`](../ST_StartPoint)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_EndPoint.java" target="_blank">Source code</a>
