# ST_AsKML

## Signatures

```sql
VARCHAR ST_AsKML(GEOMETRY geom);
VARCHAR ST_AsKML(GEOMETRY geom, BOOLEAN extrude, INT altitudeMode);
```

## Description

Converts `geom` to its [KML][wiki] representation.

The `extrude` parameter *"specifies whether to connect the LinearRing to the ground. To extrude this geometry, the altitude mode must be either `relativeToGround`, `relativeToSeaFloor`, or `absolute`. Only the vertices of the LinearRing are extruded, not the center of the geometry. The vertices are extruded toward the center of the Earth's sphere."* ([Source](https://developers.google.com/kml/documentation/kmlreference))


The `altitudeMode` parameter is used to specify a distance above the ground level, sea level, or sea floor ([See more](https://developers.google.com/kml/documentation/kmlreference)). The supported values of `altitudeMode` are:

| Value | Meaning              |
|-------|----------------------|
| 0     | none                 |
| 1     | `clampToGround`      |
| 2     | `relativeToGround`   |
| 4     | `absolute`           |
| 8     | `clampToSeaFloor`    |
| 16    | `relativeToSeaFloor` |

## Examples

#### 1. Case with `geom`

```sql
SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'));
```

Answer:

```xml
<Point>
   <coordinates>2.19,47.58</coordinates>
</Point>
```

#### 2. Case with `altitudeMode` = `clampToGround`

```sql
SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'),
                TRUE, 1);
```

Answer:

```xml
<Point>
   <extrude>1</extrude>
   <kml:altitudeMode>clampToGround</kml:altitudeMode>
   <coordinates>2.19,47.58</coordinates>
</Point>
```

#### 3. Case with `altitudeMode` = `relativeToSeaFloor`

```sql
SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'),
                FALSE, 16);
```

Answer:

```xml
<Point>
   <extrude>0</extrude>
   <gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>
   <coordinates>2.19,47.58</coordinates>
</Point>
```

#### 4. Case with `altitudeMode` = `relativeToGround`

```sql
SELECT ST_AsKML(
    ST_GeomFromText('LINESTRING(-1.53 47.24 100, -1.51 47.22 100,
                                -1.50 47.19 100, -1.49 47.17 100)',
                    4326),
    TRUE, 2);
```

Answer:

```xml
<LineString>
   <extrude>1</extrude>
   <kml:altitudeMode>relativeToGround</kml:altitudeMode>
   <coordinates>
    -1.53,47.24,100.0 -1.51,47.22,100.0 -1.5,
    47.19,100.0 -1.49,47.17,100.0
   </coordinates>
</LineString>
```

## See also

* [`KMLWrite`](../KMLWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/kml/ST_AsKml.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Keyhole_Markup_Language
