# ST_ZMin

## Signature

```sql
DOUBLE ST_ZMin(GEOMETRY geom);
```

## Description

Returns the minimum z-value of `geom`.

## Example

```sql
SELECT ST_ZMin('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    3.0
```

![](./ST_ZMin.png)

## See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_ZMin.java" target="_blank">Source code</a>

