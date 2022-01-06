--A simple SQL script to test some spatial functions
DROP TABLE IF EXISTS water;
CREATE TABLE water as select the_geom from WATERNETWORK ;
CREATE SPATIAL INDEX ON water(the_geom);