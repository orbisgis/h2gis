## Changelog for v2.2.4

- Add ST_EnvelopeAsText function
- Add ST_AsOverpassBbox function
- Add ST_OverpassDownloader function
- Add ST_SimplifyVW function
- Fix bug when read GeometryCollection with the ST_GeomFromGeoJSON function
- Fix write empty table to FGB file
- Fix github actions
- Fix mixed srid error on empty geometry with ST_Extent #1400
- Remove transitive dependency from flatgeobuffer to JTS version 1.19 (should use 1.20 of jts-core)
- Allow ST_GRAPH to be executed with empty geometry
- Allow ST_GRAPH to be executed when the input table contains an autoincrement column and not only a PK
- Fix bug when read GeometryCollection with the ST_GeomFromGeoJSON function 
- ST_MAKEGRID and ST_MAKEGRIDPOINTS : add an option to order the cells starting from the upper left corner 