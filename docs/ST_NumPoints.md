# ST_NumPoints

## Signature

```sql
INT ST_NumPoints(GEOMETRY linestring);
```

## Description

Returns the number of points in a `linestring`. 

With other dimensions (`points` and `polygons`) or `multi`geometries, `ST_NumPoints` will return a `NULL` value.

```{include} sfs-1-2-1.md
```

:::{note}
Implements the SQL/MM specification. SQL-MM 3: 7.2.4
:::


## Examples

```sql
SELECT ST_NumPoints('LINESTRING(2 2, 4 4)');
-- Answer: 2
```
ST_NumPoints includes duplicate points in the count.
```sql
SELECT ST_NumPoints('LINESTRING(2 2, 4 4, 6 6, 6 6)');
-- Answer: 4
```

### Where `ST_NumPoints` returns `NULL`
```sql
SELECT ST_NumPoints('POINT(2 2)');
-- Answer: NULL
```
```sql
SELECT ST_NumPoints('MULTIPOINT(2 2, 4 4)');
-- Answer: NULL
```
```sql
SELECT ST_NumPoints('MULTILINESTRING((2 2, 4 4), (3 1, 6 3))');
-- Answer: NULL
```
```sql
SELECT ST_NumPoints('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                             (1 1, 2 1, 2 5, 1 5, 1 1),
                             (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: NULL
```

## See also

* [`ST_NPoints`](../ST_NPoints)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_NumPoints.java" target="_blank">Source code</a>
