---
layout: docs
title: Spatial indices
prev_section: quickstart
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

{% highlight mysql %}
CREATE TABLE area(idarea int PRIMARY KEY, the_geom GEOMETRY);
CREATE SPATIAL INDEX myspatialindex ON area(the_geom);
INSERT INTO area VALUES(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))');
INSERT INTO area VALUES(2, 'POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))');
CREATE TABLE ROADS(idroad int PRIMARY KEY, the_geom GEOMETRY);
CREATE SPATIAL INDEX ON roads(the_geom);
INSERT INTO roads VALUES(1, 'LINESTRING (27.65595463138 -16.728733459357244, 47.61814744801515 40.435727788279806)');
INSERT INTO roads VALUES(2, 'LINESTRING (17.674858223062415 55.861058601134246, 55.78449905482046 76.73062381852554)');
{% endhighlight %}

{% highlight mysql %}
SELECT idarea, COUNT(idroad) roadscount
    FROM area,roads
    WHERE area.the_geom && roads.the_geom
    AND ST_Intersects(area.the_geom,roads.the_geom)
    GROUP BY idarea
    ORDER BY idarea
{% endhighlight %}

[spatial indices]: http://en.wikipedia.org/wiki/Spatial_index#Spatial_index
[syntax]: http://www.h2database.com/html/grammar.html#create_index
