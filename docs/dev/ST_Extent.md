---
layout: docs
title: ST_Extent
category: h2spatial-ext/properties
description: Return the minimum bounding box of a <code>GEOMETRYCOLLECTION</code>
prev_section: ST_Explode
next_section: ST_XMax
permalink: /docs/dev/ST_Extent/
---
 
### Signature

{% highlight mysql %}
GEOMETRY ST_Extent(GEOMETRY geom);
GEOMETRY ST_Extent(GEOMETRYCOLLECTION geom);
{% endhighlight %}

### Description

Returns the minimum bounding box that encloses `geom` as a `GEOMETRY`.

### Examples

{% highlight mysql %}
SELECT ST_Extent('MULTIPOINT((5 6), (1 2), (3 4), (10 3))'::Geometry);
-- Answer: POLYGON ((1 2, 1 6, 10 6, 10 2, 1 2))
{% endhighlight %}

<img class="displayed" src="../ST_Extent1.png"/>

{% highlight mysql %}
CREATE TABLE input_table(geom GEOMETRY);
INSERT INTO input_table VALUES 
     ('POLYGON ((0 0, 3 -1, 1.5 2, 0 0))'), 
     ('POLYGON ((2 0, 3 3, 4 2, 2 0))'), 
     ('POINT(5 6)'), 
     ('LINESTRING(1 1, 1 6)');
SELECT ST_EXTENT(geom) FROM input_table;
-- Answer: POLYGON ((0 -1, 0 6, 5 6, 5 -1, 0 -1))
{% endhighlight %}

<img class="displayed" src="../ST_Extent2.png"/>

{% highlight mysql %}
SELECT ST_Extent('POINT(5 6)'::Geometry);
-- Answer: POINT(5 6)
{% endhighlight %}

##### See also
* [`ST_Envelope`](../ST_Envelope)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_Extent.java" target="_blank">Source code</a>
