# ST_AsKML

## Signatures

```sql
VARCHAR ST_AsKML(GEOMETRY geom);
VARCHAR ST_AsKML(GEOMETRY geom, BOOLEAN extrude, INT altitudeMode);
```

## Description

Converts `geom` to its [KML][wiki] representation.

Supported values of `altitudeMode`:

| Value | Meaning              |
|-------|----------------------|
| 0     | none                 |
| 1     | `clampToGround`      |
| 2     | `relativeToGround`   |
| 4     | `absolute`           |
| 8     | `clampToSeaFloor`    |
| 16    | `relativeToSeaFloor` |

## Examples

```sql
SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'));
-- Answer: <Point>
--             <coordinates>2.19,47.58</coordinates>
--         </Point>

SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'),
                TRUE, 1);
-- Answer: <Point>
--             <extrude>1</extrude>
--             <kml:altitudeMode>clampToGround</kml:altitudeMode>
--             <coordinates>2.19,47.58</coordinates>
--          </Point>

SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'),
                FALSE, 16);
-- Answer: <Point>
--             <extrude>0</extrude>
--             <gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>
--             <coordinates>2.19,47.58</coordinates>
--         </Point>

SELECT ST_AsKML(
    ST_GeomFromText('LINESTRING(-1.53 47.24 100, -1.51 47.22 100,
                                -1.50 47.19 100, -1.49 47.17 100)',
                    4326),
    TRUE, 2);
-- Answer: <LineString>
--             <extrude>1</extrude>
--             <kml:altitudeMode>relativeToGround</kml:altitudeMode>
--             <coordinates>
--                 -1.53,47.24,100.0 -1.51,47.22,100.0 -1.5,
--                 47.19,100.0 -1.49,47.17,100.0
--             </coordinates>
--         </LineString>
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/kml/ST_AsKml.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Keyhole_Markup_Language
