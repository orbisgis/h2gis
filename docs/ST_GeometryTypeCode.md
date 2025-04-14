# ST_GeometryTypeCode

## Signature

```sql
INT ST_GeometryTypeCode(GEOMETRY geom);
```

## Description

Returns the geometry type code from the OpenGIS Simple Features
Implementation Specification for SQL <a
href="http://www.opengeospatial.org/standards/sfs"
target="_blank">version 1.2.1</a>.
Ignores *z*- and *m*-values.
For use in contraints.

| CODE |    GEOMETRY TYPE    |
| ---- | ------------------- |
|    0 | Geometry          |
|    1 | `POINT`             |
|    2 | `LINESTRING`        |
|    3 | `POLYGON`           |
|    4 | `MULTIPOINT`        |
|    5 | `MULTILINESTRING`   |
|    6 | `MULTIPOLYGON`      |
|    7 | `GEOMCOLLECTION`    |
|   13 | `CURVE`             |
|   14 | `SURFACE`           |
|   15 | `POLYHEDRALSURFACE` |

## Examples

```sql
SELECT ST_GeometryTypeCode(ST_GeomFromText('POINT(1 1)'));
-- Answer: 1

SELECT ST_GeometryTypeCode('LINESTRING(1 1, 5 5)'::Geometry);
-- Answer: 2

SELECT ST_GeometryTypeCode(
        ST_GeomFromText('MULTIPOLYGON(((1 1, 2 2, 5 3, 1 1)),
                                      ((0 0, 2 2, 5 3, 0 0)))'));
-- Answer: 6
```

## See also

* [`ST_GeometryType`](../ST_GeometryType)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_GeometryTypeCode.java" target="_blank">Source code</a>
