# ST_UpdateZ

## Signatures

```sql
GEOMETRY ST_UpdateZ(GEOMETRY geom, DOUBLE newZ);
GEOMETRY ST_UpdateZ(GEOMETRY geom, DOUBLE newZ, INT updateCondition);
```

## Description

Replaces the *z*-values of some or all of the coordinates of `geom` by `newZ`.
The optional parameter `updateCondition` determines which coordinates are updated:

| Value | Meaning                                       |
|-------|-----------------------------------------------|
| 1     | all *z*-values (by default)                   |
| 2     | all *z*-values except non-existant *z*-values |
| 3     | only non-existant *z*-values                  |

## Examples

```sql
-- Update all z-values by default:
SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10);
-- Answer:         MULTIPOINT((190 300 10), (10 11 10))

-- Update all z-values:
SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10, 1);
-- Answer:         MULTIPOINT((190 300 10), (10 11 10))

-- Update all z-values except non-existant ones:
SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10, 2);
-- Answer:         MULTIPOINT((190 300), (10 11 10))

-- Update only non-existant z-values:
SELECT ST_UpdateZ('MULTIPOINT((190 300), (10 11 2))', 10, 3);
-- Answer:         MULTIPOINT((190 300 10), (10 11 2))
```

## See also

* [`ST_ZUpdateLineExtremities`](../ST_ZUpdateLineExtremities),
  [`ST_MultiplyZ`](../ST_MultiplyZ),
  [`ST_AddZ`](../ST_AddZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_UpdateZ.java" target="_blank">Source code</a>
