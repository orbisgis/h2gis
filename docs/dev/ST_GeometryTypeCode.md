---
layout: docs
title: ST_GeometryTypeCode
category: Geometry2D/geometry-conversion
description: Return the geometry type code from OGC SFS
prev_section: ST_GeomFromText
next_section: ST_Holes
permalink: /docs/dev/ST_GeometryTypeCode/
---

### Signature

{% highlight mysql %}
INT ST_GeometryTypeCode(GEOMETRY geom);
{% endhighlight %}

### Description
Returns the geometry type code from OGC SFS 
<a href="http://www.opengeospatial.org/standards/sfs" target="_blank">version 1.2.1</a>. 
This function does not take account of z or m values.
This function is not part of SFS. It is used in constraints.

| Code |    Geometry  type   |
| ---- | ------------------- |
|    0 | GEOMETRY            |
|    1 | POINT               |
|    2 | LINESTRING          |
|    3 | POLYGON             |
|    4 | MULTIPOINT          |
|    5 | MULTILINESTRING     |
|    6 | MULTIPOLYGON        |
|    7 | GEOMCOLLECTION      |
|   13 | CURVE               |
|   14 | SURFACE             |
|   15 | POLYHEDRALSURFACE   |

### Examples

{% highlight mysql %}
SELECT ST_GeometryTypeCode(ST_Geomfromtext('POINT(1 1)'));
-- Answer: 1

SELECT ST_GeometryTypeCode('LINESTRING(1 1, 5 5)'::Geometry);
-- Answer: 2

SELECT ST_GeometryTypeCode(ST_Geomfromtext(
                            'MULTIPOLYGON(((1 1, 2 2, 5 3, 1 1)),
                                          ((0 0, 2 2, 5 3, 0 0)))'));
-- Answer: 6
{% endhighlight %}

##### See also

* [`ST_GeometryType`](../ST_GeometryType)
* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_GeometryTypeCode.java" target="_blank">Source code</a>