---
layout: docs
title: Spatial indices
prev_section: h2drivers
next_section: spatial-jdbc
permalink: /docs/dev/spatial-indices/
---

To optimize spatial queries, [spatial indices][] are supported on the Geometry
columns of regular tables (not shapefiles). The [syntax][] is the following:

{% highlight mysql %}
CREATE SPATIAL INDEX [index_name] ON table_name(geometry_column);
{% endhighlight %}

The spatial index is stored on disk.  Several spatial operators (such as the
`&&` predicate operator for bounding box overlap) use this index.

## Example

In this example, we calculate the number of roads that intersect several
polygonal areas.  First, we create the `area` and `roads` tables, putting a
spatial index on their Geometry columns:

{% highlight mysql %}
CREATE TABLE area(idarea INT PRIMARY KEY, geom GEOMETRY);
CREATE SPATIAL INDEX myspatialindex ON area(geom);
INSERT INTO area VALUES (1,
    'POLYGON((0 0, 20 0, 20 10, 0 10, 0 0))');
INSERT INTO area VALUES (2,
    'POLYGON((25 5, 40 5, 40 15, 25 15, 25 5))');
INSERT INTO area VALUES (3,
    'POLYGON((45 10, 50 10, 50 13, 45 13, 45 10))');

CREATE TABLE roads(idroad INT PRIMARY KEY, geom GEOMETRY);
CREATE SPATIAL INDEX ON roads(geom);
INSERT INTO roads VALUES (1, 'LINESTRING(2 2, 7 7)');
INSERT INTO roads VALUES (2, 'LINESTRING(15 -1, 30 13)');
{% endhighlight %}

Now we execute the request:

{% highlight mysql %}
SELECT idarea, COUNT(idroad) roadscount
    FROM area, roads
    WHERE area.geom && roads.geom
    AND ST_Intersects(area.geom, roads.geom)
    GROUP BY idarea
    ORDER BY idarea;
{% endhighlight %}

Result:

| IDAREA | ROADSCOUNT |
|--------|------------|
|      1 |          2 |
|      2 |          1 |

Note that [`ST_Intersects`](../ST_Intersects) does not yet support spatial
indices, but it will in a future release.

[spatial indices]: http://en.wikipedia.org/wiki/Spatial_index#Spatial_index
[syntax]: http://www.h2database.com/html/grammar.html#create_index
