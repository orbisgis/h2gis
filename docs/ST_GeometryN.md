# ST_GeometryN

## Signature

```sql
GEOMETRY ST_GeometryN(GEOMETRY geom, integer n);
```

## Description

Returns the *n*th Geometry of `geom` if `geom` is a `GEOMETRYCOLLECTION`,
`MULTIPOINT`, `MULTILINESTRING` or `MULTIPOLYGON`. Returns `NULL` if `geom` is
a single Geometry.

{% include one-to-n.html %}
```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_GeometryN('MULTIPOLYGON(((0 0, 3 -1, 1.5 2, 0 0)),
                                  ((1 2, 4 2, 4 6, 1 6, 1 2)))', 1);
-- Answer: POLYGON((0 0, 3 -1, 1.5 2, 0 0))

SELECT ST_GeometryN('MULTILINESTRING((1 1, 1 6, 2 2, -1 2),
                                     (1 2, 4 2, 4 6))', 2);
-- Answer: LINESTRING(1 2, 4 2, 4 6)

SELECT ST_GeometryN('MULTIPOINT((0 0), (1 6), (2 2), (1 2))', 2);
-- Answer: POINT(1 6)

SELECT ST_GeometryN('GEOMETRYCOLLECTION(
                       MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                       LINESTRING(2 6, 6 2),
                       POINT(4 4),
                       POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))', 3);
-- Answer: POINT(4 4)

-- Select Geometry 4 of the first Geometry in this GEOMETRYCOLLECTION.
SELECT ST_GeometryN(
           ST_GeometryN('GEOMETRYCOLLECTION(
                           MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                           LINESTRING(2 6, 6 2))', 1), 4);
-- Answer: POINT(0 3)

-- Returns NULL for single Geometries.
SELECT ST_GeometryN('LINESTRING(1 1, 1 6, 2 2, -1 2)', 1);
-- Answer: NULL

SELECT ST_GeometryN('MULTIPOINT((0 0), (1 6), (2 2), (1 2))', 0);
-- Answer: GEOMETRY index out of range. Must be between 1 and
-- ST_NumGeometries.
```

## See also

* [`ST_NumGeometries`](../ST_NumGeometries)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_GeometryN.java" target="_blank">Source code</a>
