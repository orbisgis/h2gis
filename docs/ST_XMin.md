# ST_XMin

## Signature

```sql
DOUBLE ST_XMin(GEOMETRY geom);
```

## Description

Returns the minimum x-value of `geom`.

## Example

```sql
SELECT ST_XMin('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    1.0
```

![](./ST_XMin.png)

## See also

* [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_XMin.java" target="_blank">Source code</a>
