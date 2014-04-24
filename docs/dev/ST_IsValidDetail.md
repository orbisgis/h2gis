---
layout: docs
title: ST_IsValidDetail
category: Geometry2D/properties
description: Return a valid_detail as an array of objects
prev_section: ST_IsValid
next_section: ST_IsValidReason
permalink: /docs/dev/ST_IsValidDetail/
---

### Signature

{% highlight mysql %}
boolean isValidDetail(GEOMETRY geom);
Object[] isValidDetail(GEOMETRY geom, int flag);
{% endhighlight %}

### Description
Returns a valid_detail as an array of objects.
The value for `flag` can be:
* [0] = isvalid equals true if the geometry is valid otherwise false (Default value),
* [1] = reason, 
* [2] = error location. 

It can have the following values (0 or 1) 1 = It will validate inverted shells and exverted holes according the ESRI SDE model. 0 = It will based on the OGC geometry model

### Examples

{% highlight mysql %}
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/847a47a2bd304a556434b89c2d31ab3ba547bcd0/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_IsValidDetail.java" target="_blank">Source code</a>

