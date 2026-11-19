# ST_AddPoint

## Signatures

```sql
GEOMETRY ST_AddPoint(GEOMETRY geom, POINT point);
GEOMETRY ST_AddPoint(GEOMETRY geom, POINT point, INTEGER index);
```

## Description

Adds `point` to the `LINESTRING` `geom`.

* Without `index`, `point` is appended at the end of `geom`.
* With `index` (0-based), `point` replaces the vertex at this position. If `index` is equal to the number of points of `geom`, `point` is appended. An error is thrown if `index` is negative or greater than the number of points.

The new vertex takes the dimension of `geom`: if `geom` has a Z (or M) value and `point` has not, the Z (or M) value is set to `0`. The Z or M value of `point` is ignored if `geom` has no Z or M.

Returns `NULL` if `geom` or `point` is `NULL`, and `geom` if one of them is empty.

:::{note}
`geom` must be a `LINESTRING`, an error is thrown for the other geometry types. To insert a point in a `POLYGON`, a `MULTILINESTRING` or a `MULTIPOLYGON`, by snapping it on the nearest segment, use [`ST_InsertPoint`](../ST_InsertPoint).
:::

## Examples

```sql
SELECT ST_AddPoint('LINESTRING(0 8, 1 8, 3 8, 8 8, 10 8, 20 8)',
                   'POINT(0 8)');
-- Answer: LINESTRING(0 8, 1 8, 3 8, 8 8, 10 8, 20 8, 0 8)
```

```sql
SELECT ST_AddPoint('LINESTRING(0 8, 1 8, 3 8, 8 8, 10 8, 20 8)',
                   'POINT(0 8)', 3);
-- Answer: LINESTRING(0 8, 1 8, 3 8, 0 8, 10 8, 20 8)
```

```sql
SELECT ST_AddPoint('LINESTRING(0 8, 1 8, 3 8, 8 8, 10 8, 20 8)',
                   'POINT(30 8)', 6);
-- Answer: LINESTRING(0 8, 1 8, 3 8, 8 8, 10 8, 20 8, 30 8)
```

```sql
SELECT ST_AddPoint('LINESTRING Z(0 8 0, 1 8 0, 3 8 0, 8 8 0, 10 8 0, 20 8 0)',
                   'POINT(0 8)', 3);
-- Answer: LINESTRING Z(0 8 0, 1 8 0, 3 8 0, 0 8 0, 10 8 0, 20 8 0)
```

```sql
SELECT ST_AddPoint('LINESTRING(0 8, 1 8, 3 8, 8 8, 10 8, 20 8)',
                   'POINT(0 8)', 8);
-- Answer: The point index is out of range (0..6): found 8
```

```sql
SELECT ST_AddPoint('POLYGON((1 1, 1 5, 5 5, 5 1, 1 1),
                            (2 2, 4 2, 4 4, 2 4, 2 2))',
                   'POINT(3 3)', 1);
-- Answer: First argument must be a LINESTRING, use ST_InsertPoint for the other geometries
```

## See also

* [`ST_InsertPoint`](../ST_InsertPoint), [`ST_RemovePoints`](../ST_RemovePoints), [`ST_Densify`](../ST_Densify)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_AddPoint.java" target="_blank">Source code</a>
