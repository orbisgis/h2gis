# ST_GeometryType

## Signature

```sql
VARCHAR ST_GeometryType(GEOMETRY geom);
```

## Description

Returns `geom`'s type.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_GeometryType('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: MultiPoint

SELECT ST_GeometryType('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: LineString

SELECT ST_GeometryType('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)),
                                     ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: MultiPolygon

SELECT ST_GeometryType('GEOMETRYCOLLECTION(
                          MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                          LINESTRING(2 6, 6 2),
                          POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: GEOMETRYCOLLECTION
```

## See also

* [`ST_Dimension`](../ST_Dimension),
  [`ST_GeometryTypeCode`](../ST_GeometryTypeCode)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_GeometryType.java" target="_blank">Source code</a>
