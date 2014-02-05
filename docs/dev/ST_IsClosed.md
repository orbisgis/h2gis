---
layout: docs
title: ST_IsClosed
category: h2spatial/properties
description: Return true if a <code>LINESTRING</code> is closed
prev_section: ST_InteriorRingN
next_section: ST_IsEmpty
permalink: /docs/dev/ST_IsClosed/
---

### Signature

{% highlight mysql %}
boolean ST_IsClosed(GEOMETRY geom);
{% endhighlight %}

### Description

Returns true if a `LINESTRING` is closed. If the input parameter is not a `LINESTRING` returns Null.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_IsClosed('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: FALSE

SELECT ST_IsClosed('LINESTRING(2 1, 1 3, 5 2, 2 1)');
-- Answer: TRUE

SELECT ST_IsClosed('MULTILINESTRING((0 2, 3 2, 3 6, 0 6, 0 1), 
                                     (5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: FALSE

SELECT ST_IsClosed('MULTILINESTRING((0 2, 3 2, 3 6, 0 6, 0 2), 
                                     (5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: TRUE
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_IsClosed.java" target="_blank">Source code</a>
