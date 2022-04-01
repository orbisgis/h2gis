## Changelog for v2.1.0

+ Fix javadoc issues.
+ Remove `slf4j-simple` dependency (#1261)
+ Fix geometry_columns view function in order to escape table name with reserved database word (#1269)
+ Add an option in ASC file reader in order to add the asc content into an existing table (#1270)
+ Change batch size to use the optimal size (#1275) 
+ Add new constructors to TableLocation (#1264)
+ Add `ST_AsEWKB` function(#1271)
+ Add `ST_Multi` function (#1268)
+ Fix GeoJSON driver on H2GIS does not use the good object type for geometry (#1277)
+ Fix estimated extend on schema (#1286)
+ `TableLocation.parse` now restore a `TableLocation.toString()` to the initial state
+ Make test run again without PostGIS instance
+ ST_DistanceSphere throws an exception when the twon input srids are different (#1292)
+ Remove OrbisParent dependency