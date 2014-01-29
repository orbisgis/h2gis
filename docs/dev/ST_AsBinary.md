---
layout: docs
title: ST_AsBinary
category: h2spatial/geometry-conversion
description: Geometry &rarr; Well Known Binary
prev_section: h2spatial/geometry-conversion
next_section: ST_AsText
permalink: /docs/dev/ST_AsBinary/
---

### Signatures

{% highlight mysql %}
binary ST_AsBinary(GEOMETRY geom);
{% endhighlight %}

### Description

Converts a `GEOMETRY` into binary value = Well Known Binary.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_AsBinary(ST_GeomFromText(
    'POLYGON((0 0,0 1,1 1,1 0,0 0))',4326));
-- Answer: 0020000003000010e600000001000000050000000000000000
--  000000000000000000000000000000003ff00000000000003ff000000
--  00000003ff00000000000003ff0000000000000000000000000000000
--  000000000000000000000000000000
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_AsBinary.java" target="_blank">Source code</a>
