# ST_OrderingEquals

## Signature

```sql
BOOLEAN ST_OrderingEquals(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

Returns `TRUE` if `geomA` and `geomB` are equal and their
coordinates and component Geometries are listed in the same order.
The condition is stronger than [`ST_Equals`](../ST_Equals).

## Examples

The same
```sql
SELECT ST_OrderingEquals('LINESTRING(0 0 1, 0 0, 10 10 3)',
                         'LINESTRING(0 0 1, 0 0, 10 10 3)');
-- Answer: TRUE
```

Different:
```sql
SELECT ST_OrderingEquals('LINESTRING(0 0, 10 10)',
                         'LINESTRING(0 0, 5 5, 10 10)');
-- Answer: FALSE
```

The same, but with opposite vertex order:
```sql
SELECT ST_OrderingEquals('POLYGON(0 0, 10 10, 10 5, 0 0)',
                         'POLYGON(0 0, 10 5, 10 10, 0 0)');
-- Answer: FALSE
```

Different:
```sql
SELECT ST_OrderingEquals('LINESTRING(0 0 1, 0 0, 10 10)',
                         'LINESTRING(0 0, 0 0, 10 10)');
-- Answer: FALSE
```

The same, but component POLYGONs are listed in opposite order:
```sql
SELECT ST_OrderingEquals('MULTIPOLYGON(((0 0, 10 10, 10 5, 0 0)),
                                       ((1 1, 2 2, 2 1, 1 1)))',
                         'MULTIPOLYGON(((1 1, 2 2, 2 1, 1 1)),
                                       ((0 0, 10 10, 10 5, 0 0)))');
-- Answer: FALSE
```

## See also

* [`ST_Equals`](../ST_Equals), [`ST_Reverse`](../ST_Reverse)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/predicates/ST_OrderingEquals.java" target="_blank">Source code</a>
