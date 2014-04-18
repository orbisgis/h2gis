---
layout: docs
title: ST_AsWKT
category: Geometry2D/geometry-conversion
description: Geometry &rarr; Well Known Text
prev_section: ST_AsText
next_section: ST_Force2D
permalink: /docs/dev/ST_AsWKT/
---

### Signatures

{% highlight mysql %}
varchar ST_AsWKT(GEOMETRY geom);
{% endhighlight %}

### Description

Converts a Geometry into its Well Known Text value.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_AsWKT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))');
-- Answer: POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_AsWKT.java" target="_blank">Source code</a>
