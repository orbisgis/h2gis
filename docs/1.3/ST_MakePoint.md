---
layout: docs
title: ST_MakePoint
category: geom2D/geometry-creation
is_function: true
description: Construct a <code>POINT</code> from two or three coordinates
prev_section: ST_MakeLine
next_section: ST_MakePolygon
permalink: /docs/1.3/ST_MakePoint/
---

### Signatures

{% highlight mysql %}
POINT ST_MakePoint(DOUBLE x, DOUBLE y);
POINT ST_MakePoint(DOUBLE x, DOUBLE y, DOUBLE z);
{% endhighlight %}

### Description

Constructs a `POINT` from `x` and `y` (and possibly `z`).

### Examples

{% highlight mysql %}
SELECT ST_MakePoint(1.4, -3.7);
-- Answer:     POINT(1.4 -3.7)
{% endhighlight %}

<img class="displayed" src="../ST_MakePoint_1.png"/>

{% highlight mysql %}
SELECT ST_MakePoint(1.4, -3.7, 6.2);
-- Answer:     POINT(1.4 -3.7 6.2)
{% endhighlight %}

<img class="displayed" src="../ST_MakePoint_2.png"/>

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/create/ST_MakePoint.java" target="_blank">Source code</a>
