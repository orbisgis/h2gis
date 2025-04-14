# ST_XMax

## Signature

```sql
DOUBLE ST_XMax(GEOMETRY geom);
```

## Description

Returns the maximum x-value of `geom`.

## Example

```sql
SELECT ST_XMax('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    4.0
```

![](./ST_XMax.png)

## See also

* [`ST_XMin`](../ST_XMin), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_XMax.java" target="_blank">Source code</a>
