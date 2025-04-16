# ST_NumGeometries

## Signatures

```sql
INT ST_NumGeometries(GEOMETRY geom);
INT ST_NumGeometries(GEOMETRYCOLLECTION geom);
```

## Description

Returns the number of Geometries in a `GEOMETRYCOLLECTION` (or
`MULTI*`). Returns 1 for single Geometries.

<!-- This function does not seem to be SFS. Is it SQL-MM? -->

## Examples

```sql
SELECT ST_NumGeometries('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 1
```

```sql
SELECT ST_NumGeometries('MULTILINESTRING(
                             (0 2, 3 2, 3 6, 0 6, 0 1),
                             (5 0, 7 0, 7 1, 5 1, 5 0))');
-- Answer: 2
```

```sql
SELECT ST_NumGeometries('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                                 (1 1, 2 1, 2 5, 1 5, 1 1),
                                 (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: 1
```

```sql
SELECT ST_NumGeometries('MULTIPOLYGON(((0 0, 10 0, 10 6, 0 6, 0 0)),
                                      ((1 1, 2 1, 2 5, 1 5, 1 1)),
                                      ((8 5, 8 4, 9 4, 9 5, 8 5)))');
-- Answer: 3
```

```sql
SELECT ST_NumGeometries('GEOMETRYCOLLECTION(
                           MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                           LINESTRING(2 6, 6 2),
                           POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 3
```

```sql
SELECT ST_NumGeometries('MULTIPOINT((0 2), (3 2), (3 6), (0 6),
                                    (0 1), (5 0), (7 0))');
-- Answer: 7
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_NumGeometries.java" target="_blank">Source code</a>
