## Changelog for v2.2.6

- Improve H2GIS-GRAALVM module
- Add github action to compile H2GIS with GraalVM 25 +
- Fix JDBCUtilities.getUniqueFieldValues was using TableLocation.parse(string) instead of TableLocation.parse(string, dbType)
- Update flatgeobuffer library, fix missing hasZ hasM in read/write .fgb files
- Drop ST_Collect aggregate function about https://github.com/orbisgis/h2gis/issues/1475
- Add ST_ClusterDBScan function
- Add ST_ClusterIntersecting function
- Add ST_ClusterWithin function
- Improve clustering functions to load only ids- 
- Fix CPU compatibility issues in GraalVM about https://github.com/orbisgis/h2gis/issues/1473
- Improve ST_CLIP to process complex polygon
- Preserve the SRID when a “SELECT ST_GEOMFROMTEXT(''POINT(0 0)'', 4326) As the_geom” query is stored in an FGB table
- Fix ST_SubDivide with empty geometry, improve performance
- Fix ST_ClustersTest with String.format 