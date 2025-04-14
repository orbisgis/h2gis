# ST_AsGeoJson

## Signature

```sql
VARCHAR ST_AsGeoJson(GEOMETRY geom);
```

## Description

Converts `geom` to its [GeoJSON][wiki] representation.
Both 2D and 3D Geometries are supported.

## Examples

```sql
SELECT ST_AsGeoJSON('POINT(1 2)');
-- Answer: {"type":"Point","coordinates":[1.0,2.0]}

SELECT ST_AsGeoJSON('POLYGON((101 345 1, 300 345 2, 300 100 2,
                              101 100 2, 101 345 1),
                             (130 300 2, 190 300 2, 190 220 2,
                              130 220 2, 130 300 2))');
-- Answer: {"type":"Polygon", "coordinates":[[[101.0, 345.0, 1.0],
--     [300.0, 345.0, 2.0], [300.0, 100.0, 2.0], [101.0, 100.0, 2.0],
--     [101.0, 345.0, 1.0]],
--    [[130.0, 300.0, 2.0], [190.0, 300.0, 2.0], [190.0, 220.0, 2.0],
--     [130.0, 220.0, 2.0], [130.0, 300.0, 2.0]]]}
```

## See also

* [`GeoJsonWrite`](../GeoJsonWrite), [`GeoJsonRead`](../GeoJsonRead), [`ST_GeomFromGeoJson`](../ST_GeomFromGeoJson)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/ST_AsGeoJSON.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
