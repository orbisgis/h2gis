# ST_GeomFromWKB

## Signature

```sql
GEOMETRY ST_GeomFromWKB(binary wkb);
GEOMETRY ST_GeomFromWKB(binary wkb, INT srid);
```

## Description

Converts the Well Known Binary `wkb` into a Geometry, optionally with spatial reference
id `srid`. Here `wkb` can specify a `POINT`, a `LINESTRING` or a `POLYGON`.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_GeomFromWKB('0101000000000000000000F03F000000000000F03F');
-- Answer: POINT(1 1)
```
```sql
SELECT ST_GeomFromWKB('000000000200000003401000000000000040080000000000004018000000000000401400000000000040200000000000004028000000000000');
-- Answer: LINESTRING (4 3, 6 5, 8 12)
```
```sql
SELECT ST_GeomFromWKB('000000000300000001000000064048800000000000403e0000000000004049000000000000403c000000000000404a800000000000403c000000000000404a8000000000004040000000000000404900000000000040400000000000004048800000000000403e000000000000');
-- Answer: POLYGON ((49 30, 50 28, 53 28, 53 32, 50 32, 49 30)) 
```

## See also

* [`ST_PointFromWKB`](../ST_PointFromWKB), [`ST_LineFromWKB`](../ST_LineFromWKB), [`ST_PolyFromWKB`](../ST_PolyFromWKB), [`ST_AsEWKB`](../ST_AsEWKB)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_GeomFromWKB.java" target="_blank">Source code</a>
