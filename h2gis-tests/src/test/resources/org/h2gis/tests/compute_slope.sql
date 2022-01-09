--This script shows how to compute the slope by parcels using a contour lines table and a landcover table

--Triangulate the contour lines
DROP TABLE IF EXISTS contour_tin;
CREATE TABLE contour_tin AS  SELECT * FROM ST_EXPLODE('(SELECT ST_DELAUNAY(ST_ACCUM(ST_UpdateZ(ST_FORCE3D(the_geom), Z))) as the_geom from contourlines)');

--Compute the slope for each triangles
DROP TABLE IF EXISTS compute_slope;
CREATE TABLE compute_slope as SELECT ST_TriangleSlope(the_geom) as slope, the_geom from contour_tin;


--Compute slope by parcels
CREATE SPATIAL INDEX ON compute_slope(the_geom);
CREATE SPATIAL INDEX ON landcover(the_geom);

DROP TABLE IF EXISTS slope_by_parcels;
CREATE TABLE slope_by_parcels as SELECT st_area(st_intersection(st_force2D(a.the_geom), st_force2D(b.the_geom))) as area_triangle,
CASE WHEN a.slope<=3 then 'level' when a.slope >3 and a.slope <5 then 'gentle' else 'moderate' end as slope_class, b.pk  FROM compute_slope as a, landcover as b where a.the_geom && b.the_geom and st_intersects(a.the_geom, b.the_geom);

DROP TABLE IF EXISTS slope_by_parcels_classified;
CREATE TABLE slope_by_parcels_classified as SELECT  CASE WHEN slope_class = 'level' then sum(area_triangle) end as level ,
CASE WHEN slope_class = 'gentle' then sum(area_triangle) end as gentle ,
CASE WHEN slope_class = 'moderate' then sum(area_triangle) end as moderate , pk
FROM slope_by_parcels group by slope_class, pk;


DROP TABLE compute_slope, contour_tin, slope_by_parcels, slope_by_parcels_classified;
