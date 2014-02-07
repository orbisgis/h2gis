---
layout: docs
title: ST_InteriorRingN
category: h2spatial/properties
description: Return the <i>n</i>th interior ring of a <code>POLYGON</code>
prev_section: ST_GeometryType
next_section: ST_IsClosed
permalink: /docs/dev/ST_InteriorRingN/
---

### Signature

{% highlight mysql %}
LINESTRING ST_InteriorRing(GEOMETRY geom, int n);
{% endhighlight %}

### Description

Returns the *n*th interior ring of `geom` if `geom` is a `POLYGON`. Returns
`NULL` otherwise.

{% include one-to-n.html %}
{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_InteriorRingN('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                                 (1 1, 2 1, 2 5, 1 5, 1 1),
                                 (8 5, 8 4, 9 4, 9 5, 8 5))', 1);
-- Answer: LINEARRING(1 1, 2 1, 2 5, 1 5, 1 1)

SELECT ST_InteriorRingN('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                                 (1 1, 2 1, 2 5, 1 5, 1 1),
                                 (8 5, 8 4, 9 4, 9 5, 8 5))', 2);
-- Answer: LINEARRING(8 5, 8 4, 9 4, 9 5, 8 5)
{% endhighlight %}

<img class="displayed" src="../ST_InteriorRingN.png"/>

{% highlight mysql %}
SELECT ST_InteriorRingN('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                                 (1 1, 2 1, 2 5, 1 5, 1 1),
                                 (8 5, 8 4, 9 4, 9 5, 8 5))', 3);
-- Answer: Interior ring index out of range. Must be between 1 and
-- ST_NumInteriorRings.

SELECT ST_InteriorRingN('POINT(1 2)', 1);
-- Answer: NULL
{% endhighlight %}

##### Comparison with [`ST_ExteriorRing`](../ST_ExteriorRing)

{% include exteriorring-interiorringn-cf.html %}

##### See also

* [`ST_ExteriorRing`](../ST_ExteriorRing)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_InteriorRingN.java" target="_blank">Source code</a>
