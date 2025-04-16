# ST_IsEmpty

## Signature

```sql
BOOLEAN ST_IsEmpty(GEOMETRY geom);
```

## Description

Returns `TRUE` if `geom` is empty.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_IsEmpty('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: FALSE
```

```sql
SELECT ST_IsEmpty('GEOMETRYCOLLECTION(
                     MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                     LINESTRING(2 6, 6 2),
                     POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: FALSE
```

```sql
SELECT ST_IsEmpty('POLYGON EMPTY');
-- Answer: TRUE
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_IsEmpty.java" target="_blank">Source code</a>
