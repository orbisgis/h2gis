---
layout: docs
title: ST_Envelope
category: geom2D/properties
is_function: true
description: Return a Geometry's envelope as a Geometry
prev_section: ST_EndPoint
next_section: ST_Explode
permalink: /docs/1.2/ST_Envelope/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_Envelope(GEOMETRY geom);
GEOMETRY ST_Envelope(GEOMETRY geom, INT srid);
GEOMETRY ST_Envelope(GEOMETRYCOLLECTION geom);
GEOMETRY ST_Envelope(GEOMETRYCOLLECTION geom, INT srid);
{% endhighlight %}

### Description

Returns the envelope of `geom` as a Geometry, optionally setting its SRID to
`srid`. The default SRID is the same as that of `geom`.

| Input type                 | Return type                                                                                 |
|----------------------------|---------------------------------------------------------------------------------------------|
| A `POINT`                  | `POINT`                                                                                     |
| A line parallel to an axis | A two-vertex `LINESTRING`                                                                   |
| Otherwise                  | A `POLYGON` whose coordinates are `(minx miny, maxx miny, maxx maxy, minx maxy, minx miny)` |

{% include sfs-1-2-1.html %}
<!-- Is this function also SQL-MM? -->

### Examples

{% highlight mysql %}
-- The envelope of a point is a point.
SELECT ST_Envelope('POINT(1 2)', 2154);
-- Answer: POINT(1 2)

-- This is a line parallel to the x-axis, so only the endpoints are
-- conserved.
SELECT ST_Envelope('LINESTRING(1 1, 5 1, 9 1)');
-- Answer: LINESTRING(1 1, 9 1)

-- (minx miny, maxx miny, maxx maxy, minx maxy, minx miny)
SELECT ST_Envelope('MULTIPOINT(1 2, 3 1, 2 2, 5 1, 1 -1)');
-- Answer: POLYGON((1 -1, 1 2, 5 2, 5 -1, 1 -1))
{% endhighlight %}

<img class="displayed" src="../ST_Envelope_1.png"/>

{% highlight mysql %}
-- (minx miny, maxx miny, maxx maxy, minx maxy, minx miny)
SELECT ST_Envelope('LINESTRING(1 2, 5 3, 2 6)');
-- Answer: POLYGON((1 2, 1 6, 5 6, 5 2, 1 2))
{% endhighlight %}

<img class="displayed" src="../ST_Envelope_2.png"/>

##### Setting or preserving the SRID

{% highlight mysql %}
-- This shows that ST_Envelope preserves the SRID of the input
-- geometry.
SELECT ST_SRID(ST_Envelope(
    ST_GeomFromText('LINESTRING(1 1, 5 5)', 27572)))
-- Answer: 27572

-- This shows that ST_Envelope can set the SRID of Envelope.
SELECT ST_SRID(ST_Envelope(
    ST_GeomFromText('LINESTRING(1 1, 5 5)', 27572), 2154))
-- Answer: 2154
{% endhighlight %}

##### Comparison with [`ST_Extent`](../ST_Extent)

{% include extent-envelope-cf.html %}

##### Comparison with [`ST_MinimumRectangle`](../ST_MinimumRectangle)

{% include minimumrectangle-envelope-cf.html %}

##### See also

* [`ST_Extent`](../ST_Extent),
  [`ST_MinimumRectangle`](../ST_MinimumRectangle),
  [`ST_OctogonalEnvelope`](../ST_OctogonalEnvelope)
* JTS [Geometry#getEnvelope][jts]
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Envelope.java" target="_blank">Source code</a>

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/geom/Geometry.html#getEnvelope()
