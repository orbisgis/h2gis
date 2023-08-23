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
