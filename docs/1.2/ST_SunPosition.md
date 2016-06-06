---
layout: docs
title: ST_SunPosition
category: geom3D/distance-functions
is_function: true
description: Compute the sun position
prev_section: ST_3DPerimeter
next_section: geom3D/properties
permalink: /docs/1.2/ST_SunPosition/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_SunPosition(POINT point);
GEOMETRY ST_SunPosition(POINT point, TIMESTAMP time);
{% endhighlight %}

### Description

Compute the sun position, according a `point` location, and return a new 2D point which coordinate is defined as:

* `x = sun azimuth` direction along the horizon, in radians, measured from north to east,

* `y = sun altitude` above the horizon, in radians, e.g. 0 at the horizon and PI/2 at the zenith.

By default, the current time sun position is returned. Optionally, the user can specify another date using the `time` parameter. 

<div class="note warning">
    <h5>The <code>point</code> coordinates have to be exprimed in the WGS84 projection system (<code>lat / long</code>)</h5>
</div>

### Examples

At the current time:

{% highlight mysql %}
SELECT ST_SunPosition('POINT(1 2)');
-- Answer: POINT (1.4378813602343208 0.4193351921603638) 
{% endhighlight %}

Or using the `time` parameter *(e.g for May 26th 2010 at 15h35'26s)* :

{% highlight mysql %}
SELECT ST_SunPosition('POINT(1 2)', '2010-05-26 15:35:26')
-- Answer: POINT (5.403682948309358 1.0249420692671862)  
{% endhighlight %}


### Use case
A specific use case, using `ST_SunPosition` and `ST_GeometryShadow` is avalaible [HERE](https://github.com/orbisgis/h2gis/wiki/3.3-Compute-building's-shadow).


##### See also

* [`ST_GeometryShadow`](../ST_GeometryShadow)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/earth/ST_SunPosition.java" target="_blank">Source code</a>
