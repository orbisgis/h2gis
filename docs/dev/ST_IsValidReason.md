---
layout: docs
title: ST_IsValidReason
category: geom2D/properties
is_function: true
description: Return text stating if a geometry is valid or not and if not valid, a reason why
prev_section: ST_IsValidDetail
next_section: ST_NumGeometries
permalink: /docs/dev/ST_IsValidReason/
---

### Signatures

{% highlight mysql %}
VARCHAR ST_IsValidReason(GEOMETRY geom);
VARCHAR ST_IsValidReason(GEOMETRY geom, INT selfTouchValid);
{% endhighlight %}

### Description

Returns a string stating if a `geom` is valid or a reason why if it
is not.

{% include selfTouchValid.html %}

{% include equivalence.html equiv='ST_IsValidReason(geom) = ARRAY_GET(ST_IsValidDetail(geom), 2)' %}

### Examples

{% highlight mysql %}
SELECT ST_IsValidReason('POLYGON((210 440, 134 235, 145 233,
                                  310 200, 340 360, 210 440))');
-- Answer: Valid Geometry

SELECT ST_IsValidReason('POLYGON((0 0, 10 0, 10 5, 6 -2, 0 0))');
-- Answer: Self-intersection at or near
--         POINT(7.142857142857143, 0.0, NaN)

SELECT ST_IsValidReason('POLYGON((1 1, 1 6, 5 1, 1 1),
                                 (3 4, 3 5, 4 4, 3 4))', 0);
-- Answer: Hole lies outside shell at or near POINT(3.0, 4.0, NaN)
{% endhighlight %}

<img class="displayed" src="../ST_IsValidReason_1.png"/>

{% highlight mysql %}
-- The next two examples show that the validation model we choose
-- is important.
SELECT ST_IsValidReason(
            'POLYGON((3 0, 0 3, 6 3, 3 0, 4 2, 2 2, 3 0))', 0);
-- Answer: Ring Self-intersection at or near POINT(3.0, 0.0, NaN)

SELECT ST_IsValidReason(
            'POLYGON((3 0, 0 3, 6 3, 3 0, 4 2, 2 2, 3 0))', 1);
-- Answer: Valid Geometry
{% endhighlight %}

<img class="displayed" src="../ST_IsValidReason_2.png"/>

##### See also

* [`ST_IsValid`](../ST_IsValid), [`ST_IsValidDetail`](../ST_IsValidDetail)
* <a href="https://github.com/irstv/H2GIS/blob/847a47a2bd304a556434b89c2d31ab3ba547bcd0/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_IsValidReason.java" target="_blank">Source code</a>
* JTS [IsValidOp][jts]

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/operation/valid/IsValidOp.html
