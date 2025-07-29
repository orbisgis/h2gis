# ST_RemoveDuplicatedCoordinates

## Signature

```sql
GEOMETRY ST_RemoveDuplicatedCoordinates(GEOMETRY geom);
```
## Description

Returns the given `geometry` without duplicated coordinates.

## Examples

```sql
SELECT ST_RemoveDuplicatedCoordinates('
             MULTIPOINT((4 4), (1 1), (1 0), (0 3), (4 4))');
-- Answer:   MULTIPOINT ((4 4), (1 1), (1 0), (0 3)) 
```

```sql
SELECT ST_RemoveDuplicatedCoordinates('
             MULTIPOINT((4 4), (1 1), (1 0), (1 1), (4 4), (0 3), (4 4))');
-- Answer:   MULTIPOINT ((4 4), (1 1), (1 0), (0 3))
```

```sql
SELECT ST_RemoveDuplicatedCoordinates('
             LINESTRING(4 4, 1 1, 1 1)');
-- Answer:   LINESTRING (4 4, 1 1)  
```

```sql
SELECT ST_RemoveDuplicatedCoordinates('
             POLYGON((4 4, 1 1, 1 1, 0 0, 4 4))');
-- Answer:   POLYGON ((4 4, 1 1, 0 0, 4 4)) 
```

```sql
SELECT ST_RemoveDuplicatedCoordinates(
        'GEOMETRYCOLLECTION(
             POLYGON((1 2, 4 2, 4 6, 1 6, 1 6, 1 2)),
             MULTIPOINT((4 4), (1 1), (1 0), (1 1)))');
-- Answer: GEOMETRYCOLLECTION (
--           POLYGON ((1 2, 4 2, 4 6, 1 6, 1 2)), 
--           MULTIPOINT ((4 4), (1 1), (1 0))) 
```

## See also

* [`ST_RemovePoints`](../ST_RemovePoints), [`ST_RemoveRepeatedPoints`](../ST_RemoveRepeatedPoints)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_RemoveDuplicatedCoordinates.java" target="_blank">Source code</a>
