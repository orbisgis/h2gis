---
layout: docs
title: ST_IsValidDetail
category: geom2D/properties
is_function: true
description: Return a valid detail as an array of objects
prev_section: ST_IsValid
next_section: ST_IsValidReason
permalink: /docs/dev/ST_IsValidDetail/
---

### Signatures

{% highlight mysql %}
ARRAY ST_IsValidDetail(GEOMETRY geom);
ARRAY ST_IsValidDetail(GEOMETRY geom, INT selfTouchValid);
{% endhighlight %}

### Description

Returns an H2 `ARRAY` containing 3 elements describing whether
`geom` is valid:

1. A `BOOLEAN` stating whether `geom` is valid or not
2. The reason why
3. The error location, or `NULL` if `geom` is valid

{% include selfTouchValid.html %}

### Examples

{% highlight mysql %}
SELECT ST_IsValidDetail('POLYGON((210 440, 134 235, 145 233,
                                  310 200, 340 360, 210 440))');
-- Answer: (TRUE, Valid Geometry, NULL)

SELECT ST_IsValidDetail(
            'POLYGON((0 0 1, 10 0 1, 10 5 1, 6 -2 1, 0 0 1))');
-- Answer: (FALSE, Self-intersection, POINT(7.142857142857143 0 1))

SELECT ST_IsValidDetail('POLYGON((1 1, 1 6, 5 1, 1 1),
                                 (3 4, 3 5, 4 4, 3 4))');
-- Answer: (FALSE, Hole lies outside shell, POINT(3 4))
{% endhighlight %}

<img class="displayed" src="../ST_IsValidDetail_1.png"/>

{% highlight mysql %}
-- The next two examples show that the validation model we choose
-- is important.
SELECT ST_IsValidDetail(
            'POLYGON((3 0, 0 3, 6 3, 3 0, 4 2, 2 2, 3 0))', 0);
-- Answer: (FALSE, Ring Self-intersection, POINT(3 0))

SELECT ST_IsValidDetail(
            'POLYGON((3 0, 0 3, 6 3, 3 0, 4 2, 2 2, 3 0))', 1);
-- Answer: (TRUE, Valid Geometry, NULL)
{% endhighlight %}

<img class="displayed" src="../ST_IsValidDetail_2.png"/>

##### See also

* [`ST_IsValid`](../ST_IsValid), [`ST_IsValidReason`](../ST_IsValidReason)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_IsValidDetail.java" target="_blank">Source code</a>
* JTS [IsValidOp][jts]

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/operation/valid/IsValidOp.html
