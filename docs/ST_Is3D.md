# ST_Is3D

## Signature

```sql
INT ST_Is3D(GEOMETRY geom);
```

## Description

Returns 1 if a `geom` has at least one z-coordinate; 0 otherwise.

## Examples

No z-coordinates
```sql
SELECT ST_Is3D('LINESTRING(1 1, 2 1, 2 2, 1 2, 1 1)'::GEOMETRY);
-- Answer: 0
```

One z-coordinate
```sql
SELECT ST_Is3D('LINESTRING(1 1 1, 2 1, 2 2, 1 2, 1 1)'::GEOMETRY);
-- Answer: 1
```

All z-coordinates
```sql
SELECT ST_Is3D('LINESTRING(1 1 1, 2 1 2, 2 2 3,
                           1 2 4, 1 1 5)'::GEOMETRY);
-- Answer: 1
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_Is3D.java" target="_blank">Source code</a>
