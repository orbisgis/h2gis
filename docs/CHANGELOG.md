## Changelog for v2.2.1
+ Next version
+ Update EPSG 2100 definition for GGRS87 / Greek Grid transformation
+ Add geojson utility to convert a resultset to JSON map
+ Update postgresql dependency to 42.3.8
+ Fix empty polygon with ST_TESSELLATE
+ Add ST_ConcaveHull function
+ Improve ST_Graph function to keep a list of columns for the edges
+ Upgrade H2 database from 2.1.214 to 2.2.220
+ Add ST_LineSubstring and ST_LineInterpolatePoint functions
+ Add ST_MaximumInscribedCircle function
+ Extend ST_CollectionExtract to filter with one or two dimensions
+ Add ST_Clip function to a [Multi]Polygon or [Multi]LineString geometry with another [Multi]Polygon or [Multi]LineString geometry
+ Add two utilities to return a list of numeric columns or the first one
+ Add ST_ForcePolygonCCW and ST_ForcePolygonCW functions
+ Add ST_MakeArcLine and ST_MakeArcPolygon functions
+ Fix ST_MakeEllipse to keep the SRID of the input point
+ Add ST_MinimumBoundingRadius function
+ Extend ST_MinimumBoundingRadius to support table or select query
+ Extend ST_MinimumBoundingRadius to copy all fields when the input data is a table or a select query
+ Add ST_Project function
+ Update H2 to 2.2.224 and SLFJ
+ Change the `h2gis-dist` main class from `org.h2.tools.Server` to `org.h2.tools.Console`
+ Set scope as test for slf4j-simple
+ Zip and unzip functions with subfolders
+ GeoJson driver must be able to read json extension
+ GeoJson handle the M ordinate. The limitation is only ZM is supported (default Z value to 0 if M is given but not Z)
+ GeoJson read the optional ID field on feature as an attribute, but currently it will be saved back into feature attribute.
+ FlatGeobuf driver. Read Write driver and FileTable. This driver does not support mixed geometry type. On write a spatial index is created by default. The spatial index on file table is not currently supported.
+ Expose FlatGeobuf driver to the IOMethods
+ FlatGeobuf must supports Unknown geometry type for SQL GEOMETRY
+ FlatGeobuf improve data types
+ FlatGeobuf enable select query on writer
+ Fix geojson about issue #1374 geojson without coordinates give h2 problems with coordinate dimension incompatibilities
+ Add ST_IsProjectedCRS and ST_IsGeographicCRS to check if the CRS is projected or geographic
