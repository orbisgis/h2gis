# ST_PointFromText

## Signature

```sql
GEOMETRY ST_PointFromText(VARCHAR wkt);
GEOMETRY ST_PointFromText(VARCHAR wkt, INT srid);
```

## Description

Converts the Well Known Text `wkt` into a Geometry, optionally with spatial reference id `srid`. 
Verifies that `wkt` does in fact specify a `POINT`.

```{include} z-coord-warning.md
```

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_PointFromText('POINT(25 89)');
-- Answer: POINT(25 89)
```

```sql
SELECT ST_PointFromText('POINT(44 31)', 101);
-- Answer: POINT(44 31)
```

```sql
SELECT ST_PointFromText('MULTIPOINT((2 3), (4 5))', 2154);
-- Answer: The provided WKT Geometry is not a POINT
```

## See also

* [`ST_MPointFromText`](../ST_MPointFromText), [`ST_LineFromText`](../ST_LineFromText), [`ST_PolyFromText`](../ST_PolyFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_PointFromText.java" target="_blank">Source code</a>
