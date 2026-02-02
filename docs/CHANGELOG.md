## Changelog for v2.2.5

- Improve H2GIS-GRAALVM module
- Add github action to compile H2GIS with GraalVM 25 +
- Fix JDBCUtilities.getUniqueFieldValues was using TableLocation.parse(string) instead of TableLocation.parse(string, dbType)
