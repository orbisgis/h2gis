---
layout: docs
title: ST_Explode
category: geom2D/properties
is_function: true
description: Explode <CODE>GEOMETRYCOLLECTION</CODE>s into multiple Geometries
prev_section: ST_Envelope
next_section: ST_Extent
permalink: /docs/dev/ST_Explode/
---

### Signature

{% highlight mysql %}
tableName[*, explod_id] ST_Explode('tableName');
tableName[*, explod_id] ST_Explode('query');
tableName[*, explod_id] ST_Explode('tableName', 'fieldName');
tableName[*, explod_id] ST_Explode('query', 'fieldName');
{% endhighlight %}

### Description
Explodes the `GEOMETRYCOLLECTION`s in the `fieldName` column of table `tableName`, or in a `query`, into multiple Geometries.

If no field name is specified, the first Geometry column is used.

The select `query` must be enclosed in parenthesis `()`.

### Examples

{% highlight mysql %}
CREATE TABLE test_point AS SELECT
    'MULTIPOINT((1 1), (2 2))'::Geometry as GEOM;

-- ST_Explode using the 'tableName'
SELECT * FROM ST_Explode('test_point');

-- or
-- ST_Explode using a 'query'
SELECT * FROM ST_Explode('(SELECT * FROM test_point 
                          WHERE ST_Dimension(GEOM)=0)');

-- Answer:
--    |     GEOM    | EXPLOD_ID |
--    | ------------|-----------|
--    | POINT(1 1)  |     1     |
--    | POINT(2 2)  |     2     |

{% endhighlight %}

<img class="displayed" src="../ST_Explode.png"/>

{% highlight mysql %}
CREATE TABLE test_point AS SELECT
    'MULTIPOINT((1 1), (2 2))'::Geometry geomA,
    'MULTIPOINT((3 3), (2 6))'::Geometry geomB;
SELECT * FROM ST_Explode('test_point', 'geomB');
-- Answer:
--    |           GEOMA           |    GEOMB    | EXPLOD_ID |
--    |---------------------------|-------------|-----------|
--    | MULTIPOINT((1 1), (2 2))  | POINT(3 3)  |      1    |
--    | MULTIPOINT((1 1), (2 2))  | POINT(2 6)  |      2    |
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_Explode.java" target="_blank">Source code</a>
