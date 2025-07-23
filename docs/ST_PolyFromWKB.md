# ST_PolyFromWKB

## Signatures

```sql
GEOMETRY ST_PolyFromWKB(binary wkb);
GEOMETRY ST_PolyFromWKB(binary wkb, INT srid);
```

## Description

Converts the Well Known Binary `wkb` into a Geometry, optionally with spatial reference id `srid`. 
Verifies that `wkb` does in fact specify a `POLYGON`.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_PolyFromWKB('000000000300000001000000064048800000000000403e0000000000004049000000000000403c000000000000404a800000000000403c000000000000404a8000000000004040000000000000404900000000000040400000000000004048800000000000403e000000000000');
-- Answer: POLYGON ((49 30, 50 28, 53 28, 53 32, 50 32, 49 30))
```

```sql
SELECT ST_PolyFromWKB('0020000003000010e600000001000000050000000000000000000000000000000000000000000000003ff00000000000003ff00000000000003ff00000000000003ff0000000000000000000000000000000000000000000000000000000000000', 2154);
-- Answer: POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))
```

## See also

* [`ST_PointFromWKB`](../ST_PointFromWKB), [`ST_LineFromWKB`](../ST_LineFromWKB), [`ST_GeomFromWKB`](../ST_GeomFromWKB), [`ST_AsEWKB`](../ST_AsEWKB)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_PolyFromWKB.java" target="_blank">Source code</a>
