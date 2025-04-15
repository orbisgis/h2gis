# ST_Disjoint

## Signature

```sql
BOOLEAN ST_Disjoint(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

Returns true if `geomA` and `geomB` are disjoint.

Disjoint means that the two geometries have no point in common.

```{include} type-warning_geometrycollection.md
```

```{include} sfs-1-2-1.md
```
```{include} spatial_indice_warning.md
```

## Examples

### Cases where `ST_Disjoint` is true

```sql
SELECT ST_Disjoint(geomA, geomB) FROM input_table;
-- Answer:    TRUE
```

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | POLYGON((6 3, 7 3, 7 6, 6 6, 6 3))  |

![](./ST_Disjoint_1.png){align=center}

| geomA LINESTRING           | geomB LINESTRING      |
|----------------------------|-----------------------|
| LINESTRING(2 1, 5 3, 2 6)  | LINESTRING(6 2, 6 6)  |

![](./ST_Disjoint_2.png){align=center}

| geomA LINESTRING           | geomB POINT |
|----------------------------|-------------|
| LINESTRING(2 1, 5 3, 2 6)  | POINT(4 5)  |

![](./ST_Disjoint_3.png){align=center}

### Cases where `ST_Disjoint` is false

```sql
SELECT ST_Disjoint(geomA, geomB) FROM input_table;
-- Answer:    FALSE
```

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | POLYGON((3 2, 6 2, 6 6, 3 6, 3 2))  |

![](./ST_Disjoint_4.png){align=center}

| geomA POLYGON                       | geomB MULTIPOLYGON                                                      |
|-------------------------------------|-------------------------------------------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | MULTIPOLYGON(((4 2, 7 2, 7 6, 4 6, 4 2)), ((0 6, 1 6, 1 7, 0 7, 0 6)))  |

![](./ST_Disjoint_5.png){align=center}

## See also

* [`ST_Contains`](../ST_Contains), [`ST_Overlaps`](../ST_Overlaps), [`ST_Touches`](../ST_Touches)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/predicates/ST_Disjoint.java" target="_blank">Source code</a>
