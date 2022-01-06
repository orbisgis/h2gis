--A simple SQL script to test some spatial functions
DROP TABLE IF EXISTS hedges_buffer;
CREATE TABLE hedges_buffer as select st_buffer(the_geom, 50) as the_geom from hedges ;
CREATE SPATIAL INDEX ON hedges_buffer(the_geom);
CREATE SPATIAL INDEX ON landcover(the_geom);

SELECT COUNT(b.*) as count, sum(st_area(b.the_geom)) as sum_landcover FROM hedges_buffer as a, landcover as b where a.the_geom && b.the_geom and st_intersects(a.the_geom, b.the_geom);

DROP TABLE hedges_buffer;