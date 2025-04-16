# ST_PointN

## Signature

```sql
POINT ST_PointN(GEOMETRY geometry, INT n);
```

## Description

Returns the <i>n</i>th point of `geom` if `geom` is a `LINESTRING` or a
`MULTILINESTRING` containing exactly one `LINESTRING`; `NULL` otherwise.

```{include} one-to-n.md
```

```{include} sfs-1-2-1.md
```

## Example

```sql
SELECT ST_PointN('LINESTRING(1 1, 1 6, 2 2, -1 2))', 2);
-- Answer: POINT(1 6)
```

```sql
SELECT ST_PointN('MULTILINESTRING((1 1, 1 6, 2 2, -1 2))', 3);
-- Answer: POINT(2 2)
```

```sql
SELECT ST_PointN('MULTIPOINT(1 1, 1 6, 2 2, -1 2)', 3);
-- Answer: NULL
```

This MULTILINESTRING contains two LINESTRINGs
```sql
SELECT ST_PointN('MULTILINESTRING((1 1, 1 6, 2 2, -1 2),
                                  (0 1, 2 4))', 3);
-- Answer: NULL
```

```sql
SELECT ST_PointN('LINESTRING(1 1, 1 6, 2 2, -1 2))', 0);
-- Answer: Point index out of range. Must be between 1 and ST_NumPoints.
```

## See also

* [`ST_StartPoint`](../ST_StartPoint), [`ST_EndPoint`](../ST_EndPoint)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_PointN.java" target="_blank">Source code</a>
