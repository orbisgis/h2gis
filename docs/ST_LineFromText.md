# ST_LineFromText

## Signatures

```sql
GEOMETRY ST_LineFromText(VARCHAR wkt);
GEOMETRY ST_LineFromText(VARCHAR wkt, INT srid);
```

## Description

{% include from-wkt-desc.html type='LINESTRING' %}
{% include z-coord-warning.html %}
```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_LineFromText('LINESTRING(2 3, 4 6, 10 6, 12 15)');
-- Answer: LINESTRING(2 3, 4 6, 10 6, 12 15)

SELECT ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)', 2154);
-- Answer: LINESTRING(5 5, 1 2, 3 4, 99 3)

SELECT ST_LineFromText('LINESTRING(0 0 -1, 2 0 2, 2 1 3)', 2154);
-- Answer: LINESTRING(0 0, 2 0, 2 1)

SELECT ST_LineFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a LINESTRING.
```

## See also

* [`ST_MLineFromText`](../ST_MLineFromText), [`ST_PointFromText`](../ST_PointFromText), [`ST_PolyFromText`](../ST_PolyFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_LineFromText.java" target="_blank">Source code</a>
