## Changelog for v2.2.5

- Improve H2GIS-GRAALVM module
- Add github action to compile H2GIS with GraalVM 25 +
- Fix JDBCUtilities.getUniqueFieldValues was using TableLocation.parse(string) instead of TableLocation.parse(string, dbType)
- Update flatgeobuffer library, fix missing hasZ hasM in read/write .fgb files
- Drop ST_Collect aggregate function about https://github.com/orbisgis/h2gis/issues/1475
- Add ST_ClusterDBScan function
- Add ST_ClusterIntersecting function
- Add ST_ClusterWithin function
