# ST_MultiplyZ

## Signature

```sql
GEOMETRY ST_MultiplyZ(GEOMETRY geom, DOUBLE zFactor);
```

## Description

Multiply the *z*-value of each coordinate of `geom` by a `zFactor`.
Non-existent *z*-values are not updated.

## Examples

```sql
SELECT ST_MultiplyZ('MULTIPOINT((190 300 1), (10 11 50))', 10);
-- Answer:           MULTIPOINT((190 300 10), (10 11 500))

-- Non-existent z-values are not updated:
SELECT ST_MultiplyZ('MULTIPOINT((190 300), (10 11))', 10);
-- Answer:           MULTIPOINT((190 300), (10 11)
SELECT ST_MultiplyZ('MULTIPOINT((190 300 10), (10 11))', 10);
-- Answer:           MULTIPOINT((190 300 100), (10 11))
```

## See also

* [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_MultiplyZ.java" target="_blank">Source code</a>
