# ST_PointFromWKB

## Signatures

```sql
GEOMETRY ST_PointFromWKB(binary wkb);
GEOMETRY ST_PointFromWKB(binary wkb, INT srid);
```

## Description

Converts the Well Known Binary `wkb` into a Geometry, optionally with spatial reference id `srid`. 
Verifies that `wkb` does in fact specify a `POINT`.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_PointFromWKB('0101000000000000000000F03F000000000000F03F');
```

Answer: POINT(1 1)
```sql
SELECT ST_PointFromWKB('000000000200000004401400000000000040140000000000003ff00000000000004000000000000000400800000000000040100000000000004058c000000000004008000000000000', 4326);
```
Answer: POINT(5 5)

```sql
SELECT ST_PointFromWKB(ST_AsBinary('LINESTRING(2 3, 4 4, 7 8)'::Geometry), 2154);
```
Answer: Provided WKB is not a POINT.

## See also

* [`ST_LineFromWKB`](../ST_LineFromWKB), [`ST_PolyFromWKB`](../ST_PolyFromWKB), [`ST_GeomFromWKB`](../ST_GeomFromWKB)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_PointFromWKB.java" target="_blank">Source code</a>
