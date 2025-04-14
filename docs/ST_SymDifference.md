# ST_SymDifference

## Signatures

```sql
GEOMETRY ST_SymDifference(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

Computes the symmetric difference between `geomA` and `geomB`.

```{include} sfs-1-2-1.md
```

## Example

| geomA Polygon                      | geomB Polygon                      |
|------------------------------------|------------------------------------|
| POLYGON((1 1, 7 1, 7 6, 1 6, 1 1)) | POLYGON((3 2, 8 2, 8 8, 3 8, 3 2)) |

```sql
SELECT ST_SymDifference(geomA, geomB) FROM input_table;
-- Answer: MULTIPOLYGON(((1 1, 7 1, 7 2, 3 2, 3 6, 1 6, 1 1)),
--                      ((7 2, 8 2, 8 8, 3 8, 3 6, 7 6, 7 2)))
```

<img class="displayed" src="../ST_SymDifference.png"/>

## See also

* [`ST_Difference`](../ST_Difference)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/operators/ST_SymDifference.java" target="_blank">Source code</a>
