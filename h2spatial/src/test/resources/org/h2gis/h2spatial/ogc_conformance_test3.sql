--//////////////////////////////////////////////////////////////////////////////
--
-- Copyright © 2010 Open Geospatial Consortium, Inc.
-- To obtain additional rights of use, visit http://www.opengeospatial.org/legal/
--
--//////////////////////////////////////////////////////////////////////////////
--
-- OpenGIS Simple Features for SQL (Types and Functions) Test Suite Software
--
-- NOTE CONCERNING INFORMATION ON CONFORMANCE TESTING AND THIS TEST SUITE
-- ----------------------------------------------------------------------
--
-- Organizations wishing to submit product for conformance testing should
-- access the above WWW site to discover the proper procedure for obtaining
-- a license to use the OpenGIS(R) certification mark associated with this
-- test suite.
--
--
-- NOTE CONCERNING TEST SUITE ADAPTATION
-- -------------------------------------
--
-- OGC recognizes that many products will have to adapt this test suite to
-- make it work properly. OGC has documented the allowable adaptations within
-- this test suite where possible. Other information about adaptations may be
-- discovered in the Test Suite Guidelines document for this test suite.
--
-- PLEASE NOTE THE OGC REQUIRES THAT ADAPTATIONS ARE FULLY DOCUMENTED USING
-- LIBERAL COMMENT BLOCKS CONFORMING TO THE FOLLOWING FORMAT:
--
-- -- !#@ ADAPTATION BEGIN
-- explanatory text goes here
-- ---------------------
-- -- BEGIN ORIGINAL SQL
-- ---------------------
-- original sql goes here
-- ---------------------
-- -- END   ORIGINAL SQL
-- ---------------------
-- -- BEGIN ADAPTED  SQL
-- ---------------------
-- adated sql goes here
-- ---------------------
-- -- END   ADAPTED  SQL
-- ---------------------
-- -- !#@ ADAPTATION END

-- -- !#@ ADAPTATION BEGIN
-- H2GIS provides its own spatial ref sys
-- CREATE TABLE spatial_ref_sys (
--
--srid INTEGER NOT NULL PRIMARY KEY,
--
--auth_name CHARACTER VARYING,
--
--auth_srid INTEGER,
--
--srtext CHARACTER VARYING(2048));
-- -- !#@ ADAPTATION END

-- Lakes

CREATE TABLE lakes (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

shore POLYGON);

-- Road Segments

CREATE TABLE road_segments (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

aliases CHARACTER VARYING(64),

num_lanes INTEGER,

centerline LINESTRING);

-- Divided Routes

CREATE TABLE divided_routes (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

num_lanes INTEGER,

centerlines MULTILINESTRING);

-- Forests

CREATE TABLE forests (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

boundary MULTIPOLYGON);

-- Bridges

CREATE TABLE bridges (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

position POINT);

-- Streams

CREATE TABLE streams (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

centerline LINESTRING);

-- Buildings

CREATE TABLE buildings (

fid INTEGER NOT NULL PRIMARY KEY,

address CHARACTER VARYING(64),

position POINT,

footprint POLYGON);

-- Ponds
CREATE TABLE ponds (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

type CHARACTER VARYING(64),

shores MULTIPOLYGON);

-- Named Places

CREATE TABLE named_places (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

boundary POLYGON);

-- Map Neatline

CREATE TABLE map_neatlines (

fid INTEGER NOT NULL PRIMARY KEY,

neatline POLYGON);

-- Spatial Reference System
-- -- !#@ ADAPTATION BEGIN
-- Adapted sql add column name. H2GIS may insert additional columns later for PostGIS compatibility
-- ---------------------
-- -- BEGIN ORIGINAL SQL
-- ---------------------
--INSERT INTO spatial_ref_sys VALUES(101, 'POSC', 32214,
--
--'PROJCS["UTM_ZONE_14N", GEOGCS["World Geodetic System
--
--72",DATUM["WGS_72", ELLIPSOID["NWL_10D", 6378135,
--
--298.26]],PRIMEM["Greenwich",
--
--0],UNIT["Meter",1.0]],PROJECTION["Transverse_Mercator"],
--
--PARAMETER["False_Easting", 500000.0],PARAMETER["False_Northing",
--
--0.0],PARAMETER["Central_Meridian", -99.0],PARAMETER["Scale_Factor",
--
--0.9996],PARAMETER["Latitude_of_origin", 0.0],UNIT["Meter", 1.0]]');
-- ---------------------
-- -- END   ORIGINAL SQL
-- ---------------------
-- -- BEGIN ADAPTED  SQL
-- ---------------------

INSERT INTO spatial_ref_sys(srid,auth_name,auth_srid,srtext) VALUES(101, 'POSC', 32214,

'PROJCS["UTM_ZONE_14N", GEOGCS["World Geodetic System

72",DATUM["WGS_72", ELLIPSOID["NWL_10D", 6378135,

298.26]],PRIMEM["Greenwich",

0],UNIT["Meter",1.0]],PROJECTION["Transverse_Mercator"],

PARAMETER["False_Easting", 500000.0],PARAMETER["False_Northing",

0.0],PARAMETER["Central_Meridian", -99.0],PARAMETER["Scale_Factor",

0.9996],PARAMETER["Latitude_of_origin", 0.0],UNIT["Meter", 1.0]]');

-- ---------------------
-- -- END   ADAPTED  SQL
-- ---------------------
-- -- !#@ ADAPTATION END

-- Lakes
-- -- !#@ ADAPTATION BEGIN
-- The lake name is inserted in capital letters however tests use another case.
-- ---------------------
-- -- BEGIN ORIGINAL SQL
-- ---------------------
-- INSERT INTO lakes VALUES (
--   101, 'BLUE LAKE',
--   ST_PolyFromText(
--   'POLYGON(
--   (52 18,66 23,73 9,48 6,52 18),
--   (59 18,67 18,67 13,59 13,59 18)
--   )',
--   101));
-- ---------------------
-- -- END   ORIGINAL SQL
-- ---------------------
-- -- BEGIN ADAPTED  SQL
-- ---------------------
INSERT INTO lakes VALUES (

101, 'Blue Lake',

ST_PolyFromText(

'POLYGON(

(52 18,66 23,73 9,48 6,52 18),

(59 18,67 18,67 13,59 13,59 18)

)',

101));
-- ---------------------
-- -- END   ADAPTED  SQL
-- ---------------------
-- -- !#@ ADAPTATION END


-- Road segments

INSERT INTO road_segments VALUES(102, 'Route 5', NULL, 2,

ST_LineFromText(

'LINESTRING( 0 18, 10 21, 16 23, 28 26, 44 31 )' ,101));

INSERT INTO road_segments VALUES(103, 'Route 5', 'Main Street', 4,

ST_LineFromText(

'LINESTRING( 44 31, 56 34, 70 38 )' ,101));

INSERT INTO road_segments VALUES(104, 'Route 5', NULL, 2,

ST_LineFromText(

'LINESTRING( 70 38, 72 48 )' ,101));

INSERT INTO road_segments VALUES(105, 'Main Street', NULL, 4,

ST_LineFromText(

'LINESTRING( 70 38, 84 42 )' ,101));

INSERT INTO road_segments VALUES(106, 'Dirt Road by Green Forest', NULL,

1,

ST_LineFromText(

'LINESTRING( 28 26, 28 0 )',101));

-- DividedRoutes

INSERT INTO divided_routes VALUES(119, 'Route 75', 4,

ST_MLineFromText(

'MULTILINESTRING((10 48,10 21,10 0),

(16 0,16 23,16 48))', 101));

-- Forests

INSERT INTO forests VALUES(109, 'Green Forest',

ST_MPolyFromText(

'MULTIPOLYGON(((28 26,28 0,84 0,84 42,28 26),

(52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))',

101));

-- Bridges

INSERT INTO bridges VALUES(110, 'Cam Bridge', ST_PointFromText(

'POINT( 44 31 )', 101));

-- Streams

INSERT INTO streams VALUES(111, 'Cam Stream',

ST_LineFromText(

'LINESTRING( 38 48, 44 41, 41 36, 44 31, 52 18 )', 101));

INSERT INTO streams VALUES(112, NULL,

ST_LineFromText(

'LINESTRING( 76 0, 78 4, 73 9 )', 101));

-- Buildings

INSERT INTO buildings VALUES(113, '123 Main Street',

ST_PointFromText(

'POINT( 52 30 )', 101),

ST_PolyFromText(

'POLYGON( ( 50 31, 54 31, 54 29, 50 29, 50 31) )', 101));

INSERT INTO buildings VALUES(114, '215 Main Street',

ST_PointFromText(

'POINT( 64 33 )', 101),

ST_PolyFromText(

'POLYGON( ( 66 34, 62 34, 62 32, 66 32, 66 34) )', 101));

-- Ponds
INSERT INTO ponds VALUES(120, NULL, 'Stock Pond',

ST_MPolyFromText(

'MULTIPOLYGON( ( ( 24 44, 22 42, 24 40, 24 44) ),

( ( 26 44, 26 40, 28 42, 26 44) ) )', 101));

-- Named Places

INSERT INTO named_places VALUES(117, 'Ashton',

ST_PolyFromText(

'POLYGON( ( 62 48, 84 48, 84 30, 56 30, 56 34, 62 48) )', 101));

INSERT INTO named_places VALUES(118, 'Goose Island',

ST_PolyFromText(

'POLYGON( ( 67 13, 67 18, 59 18, 59 13, 67 13) )', 101));

-- Map Neatlines

INSERT INTO map_neatlines VALUES(115,

ST_PolyFromText(

'POLYGON( ( 0 0, 0 48, 84 48, 84 0, 0 0 ) )', 101));


