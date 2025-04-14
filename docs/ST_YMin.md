# ST_YMin

## Signature

```sql
DOUBLE ST_YMin(GEOMETRY geom);
```

## Description

Returns the minimum y-value of `geom`.

## Examples

```sql
SELECT ST_YMin('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    2.0
```

![](./ST_YMin.png){align=center}

## See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_YMin.java" target="_blank">Source code</a>
