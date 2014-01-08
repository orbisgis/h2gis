---

layout: docs

title: ST_3DLength

prev_section: h2spatial-ext/properties

next_section: h2spatial-ext/ST_CompactnessRatio

permalink: /docs/dev/h2spatial-ext/ST_3DLength/

---
 
### Signature

{% highlight mysql %}
double ST_3DLength(Geometry geom);
{% endhighlight %}

### Description

Returns the 3D length (of a `LINESTRING`) or the 3D perimeter (of a `POLYGON`).
In the case of a 2D geometry, `ST_3DLength` returns the same value as
`ST_Length`.

### Examples

{% highlight mysql %}
SELECT ST_3DLength('LINESTRING(1 4, 15 7, 16 17)'::Geometry);
-- Answer:    24.367696684397245 = SQRT(205) + SQRT(101)

SELECT ST_3DLength('LINESTRING(1 4 3, 15 7 9, 16 17 22)'::Geometry);
-- Answer:    31.955851421415005 = SQRT(241) + SQRT(270)

SELECT ST_3DLength('MULTILINESTRING((1 4 3, 15 7 9, 16 17 22),
                                    (0 0 0, 1 0 0, 1 2 0, 0 2 1))'::Geometry);
-- Answer:    36.3700649837881 = SQRT(241) + SQRT(270) + 3 + SQRT(2)

SELECT ST_3DLength('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))'::Geometry);
-- Answer:    6.0

SELECT ST_3DLength('POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))'::Geometry);
-- Answer:    9.048627177541054 = SQRT(2) + 2 * SQRT(5) + SQRT(10)

SELECT ST_3DLength('MULTIPOLYGON(((0 0 0, 3 2 0, 3 2 2, 0 0 2, 0 0 0),
                                  (-1 1 0, -1 3 0, -1 3 4, -1 1 4, -1 1 0)))'::Geometry);
-- Answer:    23.21110255092798 = 16 + 2 * SQRT(13)

SELECT ST_3DLength('GEOMETRYCOLLECTION(LINESTRING(1 4 3, 15 7 9, 16 17 22),
                                       POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))'::Geometry);
-- Answer:    41.004478598956055 = SQRT(241) + SQRT(270) + SQRT(2) + 2 * SQRT(5) + SQRT(10)
{% endhighlight %}

##### See also

* [Source code](https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_3DLength.java)
* Added: [#29](https://github.com/irstv/H2GIS/pull/29)
