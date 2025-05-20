# ST_MemSize

## Signature

```sql
DOUBLE ST_MemSize(GEOMETRY geom);
```

## Description

Returns the amount of memory space (in bytes) taken by the geometry `geom`.

:::{warning}
`ST_MemSize` only support geometry value
:::

## Examples

```sql
SELECT ST_MemSize('MULTIPOLYGON(((2 1, 1 2, 2 2, 2 3, 3 3, 3 2, 4 2, 4 1, 3 0, 2 0, 2 1)), 
                                ((1 3, 0 4, 3 4, 1 3.5, 1 3)))'::GEOMETRY);
-- Answer: 291
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_MemSize.java" target="_blank">Source code</a>
