---
layout: docs
title: ST_LineMerge
category: geom2D/process-geometries
is_function: true
description: Merges a collection of linear components to form maximal-length <code>LINESTRING</code>
prev_section: ST_LineIntersector
next_section: ST_MakeValid
permalink: /docs/dev/ST_LineMerge/
---

### Signatures

{% highlight mysql %}
MULTILINESTRING ST_LineMerge(GEOMETRY geom);
{% endhighlight %}

### Description

Merges a collection of `LINESTRING` elements in order to create a new collection of maximal-length `LINESTRING`s. 

If the user provide something else than `(MULTI)LINESTRING` it returns an `EMPTY MULTILINESTRING`.

### Examples

##### Case with a `LINESTRING`

{% highlight mysql %}
SELECT ST_LineMerge('LINESTRING (1 1, 1 4)') as GEOM;
-- Answer: MULTILINESTRING ((1 1, 1 4)) 
{% endhighlight %}

##### Case with a `MULTILINESTRING`

{% highlight mysql %}
SELECT ST_LineMerge('MULTILINESTRING ((1 1, 1 4), 
  				      (1 4, 5 4), 
  				      (5 4, 5 1), 
  				      (3 3, 3 4))') as GEOM;
-- Answer: MULTILINESTRING ((1 1, 1 4, 5 4, 5 1), 
--                          (3 3, 3 4)) 
{% endhighlight %}
<img class="displayed" src="../ST_LineMerge_1.png"/>

##### Case with mixed dimension geometries

{% highlight mysql %}
SELECT ST_LineMerge('GEOMETRYCOLLECTION (
			LINESTRING (1 1, 1 4), 
			POLYGON ((2 4, 4 4, 4 2, 2 2, 2 4)))');
-- Answer: MULTILINESTRING EMPTY
{% endhighlight %}

##### See also

* [`ST_Simplify`](../ST_Simplify)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/aggregate/ST_LineMerge.java" target="_blank">Source code</a>
