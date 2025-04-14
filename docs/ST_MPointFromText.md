# ST_MPointFromText

## Signature

```sql
GEOMETRY ST_MPointFromText(VARCHAR wkt);
GEOMETRY ST_MPointFromText(VARCHAR wkt, INT srid);
```

## Description

{% include from-wkt-desc.html type='MULTIPOINT' %}
{% include z-coord-warning.html %}
```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_MPointFromText('MULTIPOINT(4 2, 3 7, 6 8)');
-- Answer: MULTIPOINT((4 2), (3 7), (6 8))

SELECT ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 20 3)', 2154);
-- Answer: MULTIPOINT((5 5), (1 2), (3 4), (20 3))

SELECT ST_MPointFromText('POINT(2 3)', 2154);
-- Answer: The provided WKT Geometry is not a MULTIPOINT.
```

## See also

* [`ST_PointFromText`](../ST_PointFromText), [`ST_MLineFromText`](../ST_MLineFromText), [`ST_MPolyFromText`](../ST_MPolyFromText)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_MPointFromText.java" target="_blank">Source code</a>
