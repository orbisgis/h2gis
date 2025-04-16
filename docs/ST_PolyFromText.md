# ST_PolyFromText

## Signatures

```sql
GEOMETRY ST_PolyFromText(VARCHAR wkt);
GEOMETRY ST_PolyFromText(VARCHAR wkt, INT srid);
```

## Description

Converts the Well Known Text `wkt` into a Geometry, optionally with spatial reference id `srid`. 
Verifies that `wkt` does in fact specify a `POLYGON`.

```{include} z-coord-warning.md
```

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_PolyFromText('POLYGON ((49 30, 50 28, 53 28, 53 32, 50 32, 49 30))');
-- Answer: POLYGON ((49 30, 50 28, 53 28, 53 32, 50 32, 49 30))
```

```sql
SELECT ST_PolyFromText('POLYGON((50 31, 54 31, 54 29, 50 29, 50 31))', 2154);
-- Answer: POLYGON((50 31, 54 31, 54 29, 50 29, 50 31))
```

```sql
SELECT ST_PolyFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a POLYGON
```

## See also

* [`ST_MPolyFromText`](../ST_MPolyFromText), [`ST_PointFromText`](../ST_PointFromText), [`ST_LineFromText`](../ST_LineFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_PolyFromText.java" target="_blank">Source code</a>
