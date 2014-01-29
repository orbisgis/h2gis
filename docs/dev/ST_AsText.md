---
layout: docs
title: ST_AsText
category: h2spatial/geometry-conversion
description: Alias for <a href="/docs/dev/ST_AsWKT"><code>ST_AsWKT</code></a>
prev_section: ST_AsBinary
next_section: ST_AsWKT
permalink: /docs/dev/ST_AsText/
---

### Signatures

{% highlight mysql %}
varchar ST_AsText(GEOMETRY geom);
{% endhighlight %}

### Description

`ST_AsText` is an alias for ST_AsWKT.
Can you see <a href="/docs/dev/ST_AsWKT">`ST_AsWKT`</a>.

Convert a `GEOMETRY` value into a Well Known Text value.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_AsText('POLYGON((0 0,0 1,1 1,1 0,0 0))');
-- Answer: POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))

{% endhighlight %}

##### See also

* [`ST_AsWKT`](../ST_AsWKT)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_AsText.java" target="_blank">Source code</a>
