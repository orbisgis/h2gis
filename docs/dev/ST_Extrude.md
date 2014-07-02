---
layout: docs
title: ST_Extrude
category: geom3D/geometry-creation
is_function: true
description: Extrude a geometry
prev_section: geom3D/geometry-creation
next_section: geom3D/edit-geometries
permalink: /docs/dev/ST_Extrude/
---

### Signatures

{% highlight mysql %}
GEOMETRYCOLLECTION ST_Extrude(GEOMETRY geom, DOUBLE height);
GEOMETRYCOLLECTION ST_Extrude(GEOMETRY geom, DOUBLE height,
                              INT flag);
{% endhighlight %}

### Description

Extrudes `geom` by `height`, returning a `GEOMETRYCOLLECTION`
containing the floor (`geom`), the walls and the roof.

To extract walls, set `flag=1`; to extract the roof, set `flag=2`.

### Examples

{% highlight mysql %}
SELECT ST_Extrude('LINESTRING(1 1, 4 4)', 5);
-- Answer: GEOMETRYCOLLECTION(
--    (floor)      LINESTRING(1 1 0, 4 4 0),
--    (wall)       MULTIPOLYGON(((1 1 0, 1 1 5, 4 4 5,
--                                4 4 0, 1 1 0))),
--    (roof)       LINESTRING(1 1 5, 4 4 5))
{% endhighlight %}

<img class="displayed" src="../ST_Extrude_1.png"/>

{% highlight mysql %}
SELECT ST_Extrude('POLYGON((0 0, 3 0, 3 3, 0 3, 0 0))', 5);
-- Answer: GEOMETRYCOLLECTION(
--             POLYGON((0 0 0, 0 3 0, 3 3 0, 3 0 0, 0 0 0)),
--             MULTIPOLYGON(((0 0 0, 0 0 5, 0 3 5, 0 3 0, 0 0 0)),
--                          ((0 3 0, 0 3 5, 3 3 5, 3 3 0, 0 3 0)),
--                          ((3 3 0, 3 3 5, 3 0 5, 3 0 0, 3 3 0)),
--                          ((3 0 0, 3 0 5, 0 0 5, 0 0 0, 3 0 0))),
--             POLYGON((0 0 5, 3 0 5, 3 3 5, 0 3 5, 0 0 5)))
{% endhighlight %}

<img class="displayed" src="../ST_Extrude_2.png"/>

###### POLYGON with hole:

{% highlight mysql %}
SELECT ST_Extrude('POLYGON((0 10, 10 10, 10 0, 0 0, 0 10),
                      (1 3, 3 3, 3 1, 1 1, 1 3))', 10);
-- Answer: GEOMETRYCOLLECTION(
--             POLYGON((0 10 0, 10 10 0, 10 0 0, 0 0 0,
--                       0 10 0),
--                      (1 3, 1 1, 3 1, 3 3, 1 3)),
--             MULTIPOLYGON(((0 10 0, 0 10 10, 10 10 10,
--                            10 10 0, 0 10 0)),
--                          ((10 10 0, 10 10 10, 10 0 10,
--                            10 0 0, 10 10 0)),
--                          ((10 0 0, 10 0 10, 0 0 10,
--                            0 0 0, 10 0 0)),
--                          ((0 0 0, 0 0 10, 0 10 10,
--                            0 10 0, 0 0 0)),
--                          ((1 3 0, 1 3 10, 1 1 10, 1 1 0,
--                            1 3 0)),
--                          ((1 1 0, 1 1 10, 3 1 10, 3 1 0,
--                            1 1 0)),
--                          ((3 1 0, 3 1 10, 3 3 10, 3 3 0,
--                            3 1 0)),
--                          ((3 3 0, 3 3 10, 1 3 10,
--                            1 3 0, 3 3 0))),
--             POLYGON((0 10 10, 0 0 10, 10 0 10, 10 10 10, 0 10 10),
--                (1 3 10, 3 3 10, 3 1 10, 1 1 10, 1 3 10))))
{% endhighlight %}

###### ST_Extrude with flag parameter:

{% highlight mysql %}
SELECT ST_Extrude('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))', 10, 1);
-- Answer: MULTIPOLYGON(((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)),
--            ((0 1 0, 0 1 10, 1 1 0, 1 1 10, 0 1 0)),
--            ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)),
--            ((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0))))

SELECT ST_Extrude('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))', 10, 2);
-- Answer: POLYGON((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/create/ST_Extrude.java" target="_blank">Source code</a>
