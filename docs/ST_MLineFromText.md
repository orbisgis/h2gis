# ST_MLineFromText

## Signatures

```sql
GEOMETRY ST_MLineFromText(VARCHAR wkt);
GEOMETRY ST_MLineFromText(VARCHAR wkt, INT srid);
```

## Description

Converts the Well Known Text `wkt` into a Geometry, optionally with spatial reference id `srid`. 
Verifies that `wkt` does in fact specify a `MULTILINESTRING`.

```{include} z-coord-warning.md
```

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_MLineFromText('MULTILINESTRING((1 5, 4 6, 7 5),
                                         (2 4, 4 5, 6 3))');
-- Answer: MULTILINESTRING((1 5, 4 6, 7 5), 
--                         (2 4, 4 5, 6 3))
```

```sql
SELECT ST_MLineFromText('MULTILINESTRING((10 48, 10 21, 10 0),
                                         (16 0, 16 23, 16 48))', 101);
-- Answer: MULTILINESTRING((10 48, 10 21, 10 0),
--                         (16 0, 16 23, 16 48))
```

```sql
SELECT ST_MLineFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a MULTILINESTRING.
```

## See also

* [`ST_LineFromText`](../ST_LineFromText), [`ST_MPointFromText`](../ST_MPointFromText), [`ST_MPolyFromText`](../ST_MPolyFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_MLineFromText.java" target="_blank">Source code</a>
