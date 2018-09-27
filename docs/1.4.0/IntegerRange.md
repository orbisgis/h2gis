---
layout: docs
title: IntegerRange
category: system/version
is_function: true
description: Return an array of integers
prev_section: H2GISversion
next_section: applications
permalink: /docs/1.4.0/IntegerRange/
---

### Signatures

{% highlight mysql %}
INTEGER IntegerRange(DOUBLE begin, DOUBLE end);
INTEGER IntegerRange(DOUBLE begin, DOUBLE end, DOUBLE step);
{% endhighlight %}

### Description

Return an array of integers within the range [`start`-`end`[.

The default increment value is 1 but the user can set another one specifying the `step` parameter.

##### Remarks

* `end` is excluded from the array,
* `end` has to be greater than `begin` otherwise `IntegerRange` will throw an exception.

### Examples

{% highlight mysql %}
SELECT IntegerRange(2, 7);
-- Answer:
	2, 3, 4, 5, 6

SELECT IntegerRange(0, 6, 2);
-- Answer:
	0, 2, 4

SELECT IntegerRange(0, 1, 0.5);
-- Answer:
	0

SELECT IntegerRange(0, 4, 0.5);
-- Answer:
	0, 1, 2, 3
{% endhighlight %}

##### See also

* [`DoubleRange`](../DoubleRange)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/system/IntegerRange.java" target="_blank">Source code</a>
