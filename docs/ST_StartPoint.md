# ST_StartPoint

## Signature

```sql
POINT ST_StartPoint(GEOMETRY geom);
```

## Description

Returns the first coordinate of `geom` as a `POINT`, given that `geom` is a
`LINESTRING` or a `MULTILINESTRING` containing only one `LINESTRING`. Returns
`NULL` for all other Geometries.

```{include} sfs-1-2-1.md
```

## Example

```sql
SELECT ST_StartPoint('LINESTRING(1 2, 5 3, 2 6)');
-- Answer: POINT(1 2)
```

<img class="displayed" src="../ST_StartPoint.png"/>

```sql
SELECT ST_StartPoint('MULTILINESTRING((1 1, 3 2, 3 1))');
-- Answer: POINT(1 1)

SELECT ST_StartPoint('MULTILINESTRING((1 1, 3 2, 3 1),
                                      (1 2, 5 3, 2 6))');
-- Answer: NULL
```

## See also

* [`ST_EndPoint`](../ST_EndPoint), [`ST_PointN`](../ST_PointN)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_StartPoint.java" target="_blank">Source code</a>
