# ST_NumInteriorRings

## Signatures

```sql
INT ST_NumInteriorRings(GEOMETRY geom);
```

## Description

Return the number of interior rings of the first `POLYGON` in the geometry. 
This will work with both `POLYGON` and `MULTIPOLYGON`.
Return `NULL` if there is no polygon in the geometry.

<!-- This function does not seem to be SFS. Is it SQL-MM? -->

## Examples

```sql
SELECT ST_NumInteriorRings('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                                    (1 1, 2 1, 2 5, 1 5, 1 1),
                                    (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: 2
```

```sql
SELECT ST_NumInteriorRings('MULTIPOLYGON(
                                ((0 0, 10 0, 10 6, 0 6, 0 0),
                                ((1 1, 2 1, 2 5, 1 5, 1 1)),
                                ((8 5, 8 4, 9 4, 9 5, 8 5)))');
-- Answer: 0
```

```sql
SELECT ST_NumInteriorRings('MULTIPOLYGON(
                                ((0 0, 10 0, 10 6, 0 6, 0 0),
                                 (1 1, 2 1, 2 5, 1 5, 1 1)),
                                ((1 1, 2 1, 2 5, 1 5, 1 1)),
                                ((8 5, 8 4, 9 4, 9 5, 8 5)))');
-- Answer: 1
```

```sql
SELECT ST_NumInteriorRings(
     'GEOMETRYCOLLECTION(
        MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
        LINESTRING(2 6, 6 2),
        POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 0
```

```sql
SELECT ST_NumInteriorRings(
     'GEOMETRYCOLLECTION(
        MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
        LINESTRING(2 6, 6 2),
        POLYGON((1 2, 4 2, 4 6, 1 6, 1 2),
                (2 4, 3 4, 3 5, 2 5, 2 4)))');
-- Answer: 1
```

## See also

* [`ST_NumInteriorRing`](../ST_NumInteriorRing)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_NumInteriorRings.java" target="_blank">Source code</a>
