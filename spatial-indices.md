---

layout: docs

title: Spatial indices

prev_section: dev/quickstart

next_section: dev/spatial-jdbc

permalink: /docs/dev/spatial-indices/

---

On regular tables (not shapes) you can add a spatial index (stored on disk):

{% highlight sql %}
CREATE TABLE area(idarea int PRIMARY KEY, the_geom GEOMETRY);
CREATE SPATIAL INDEX myspatialindex ON area(the_geom);
INSERT INTO area VALUES(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))');
INSERT INTO area VALUES(2, 'POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))');
CREATE TABLE ROADS(idroad int PRIMARY KEY, the_geom GEOMETRY);
CREATE SPATIAL INDEX ON roads(the_geom);
INSERT INTO roads VALUES(1, 'LINESTRING (27.65595463138 -16.728733459357244, 47.61814744801515 40.435727788279806)');
INSERT INTO roads VALUES(2, 'LINESTRING (17.674858223062415 55.861058601134246, 55.78449905482046 76.73062381852554)');
{% endhighlight %}

The spatial predicate operator `&&` for bounding box overlap uses this index:

{% highlight sql %}
SELECT idarea, COUNT(idroad) roadscount
    FROM area,roads
    WHERE area.the_geom && roads.the_geom
    AND ST_Intersects(area.the_geom,roads.the_geom)
    GROUP BY idarea
    ORDER BY idarea
{% endhighlight %}

