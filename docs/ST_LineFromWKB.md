# ST_LineFromWKB

## Signatures

```sql
GEOMETRY ST_LineFromWKB(binary wkb);
GEOMETRY ST_LineFromWKB(binary wkb, INT srid);
```

## Description

Converts the Well Known Binary `wkb` into a Geometry, optionally with spatial reference id `srid`. 
Verifies that `wkb` does in fact specify a `LINESTRING`.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_LineFromWKB('000000000200000003401000000000000040080000000000004018000000000000401400000000000040200000000000004028000000000000');
```
Answer: LINESTRING (4 3, 6 5, 8 12)

```sql
SELECT ST_LineFromWKB('000000000200000004401400000000000040140000000000003ff00000000000004000000000000000400800000000000040100000000000004058c000000000004008000000000000', 4326);
```
Answer: LINESTRING(5 5, 1 2, 3 4, 99 3)

```sql
SELECT ST_LineFromWKB(ST_AsBinary('POINT(2 3)'::Geometry), 2154);
```
Answer: Provided WKB is not a LINESTRING.

## See also

* [`ST_PointFromWKB`](../ST_PointFromWKB), [`ST_PolyFromWKB`](../ST_PolyFromWKB), [`ST_GeomFromWKB`](../ST_GeomFromWKB)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_LineFromWKB.java" target="_blank">Source code</a>
