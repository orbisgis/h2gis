# ST_Within

## Signature

```sql
BOOLEAN ST_Within(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

Returns true if `geomA` is within `geomB`.

Within means that every point of `geomA` is a point of `geomB`, and the
interiors of the two geometries have at least one point in common.

As a consequence, the following are equivalent:

* `ST_Within(geomA, geomB)` and `ST_Within(geomB, geomA)` are true.
* `ST_Equals(geomA, geomB)` is true.

```{include} type-warning_geometrycollection.md
```

```{include} sfs-1-2-1.md
```
{% include spatial_indice_warning.html %}

## Examples

### Cases where `ST_Within` is true

```sql
SELECT ST_Within(geomA, geomB) FROM input_table;
-- Answer:    TRUE
```

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))  | POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  |

![](./ST_Within_1.png){align=center}

| geomA LINESTRING      | geomB POLYGON                       |
|-----------------------|-------------------------------------|
| LINESTRING(2 6, 6 2)  | POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  |

![](./ST_Within_2.png){align=center}

| geomA POINT | geomB POLYGON                       |
|-------------|-------------------------------------|
| POINT(4 4)  | POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  |

![](./ST_Within_3.png){align=center}

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 2, 6 2, 6 5, 1 5, 1 2))  | POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  |

![](./ST_Within_4.png){align=center}

| geomA LINESTRING           | geomB POLYGON                       |
|----------------------------|-------------------------------------|
| LINESTRING(1 2, 1 6, 5 2)  | POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  |

![](./ST_Within_5.png){align=center}

| geomA LINESTRING      | geomB LINESTRING           |
|-----------------------|----------------------------|
| LINESTRING(3 5, 5 3)  | LINESTRING(2 1, 5 3, 2 6)  |

![](./ST_Within_6.png){align=center}

| geomA POINT | geomB LINESTRING           |
|-------------|----------------------------|
| POINT(4 4)  | LINESTRING(2 1, 5 3, 2 6)  |

![](./ST_Within_7.png){align=center}

### Cases where `ST_Within` is false

```sql
SELECT ST_Within(geomA, geomB) FROM input_table;
-- Answer:    FALSE
```

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((0 2, 5 2, 5 5, 0 5, 0 2))  | POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  |

![](./ST_Within_8.png){align=center}

| geomA LINESTRING      | geomB POLYGON                       |
|-----------------------|-------------------------------------|
| LINESTRING(2 6, 0 8)  | POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  |

![](./ST_Within_9.png){align=center}

| geomA POINT | geomB POLYGON                       |
|-------------|-------------------------------------|
| POINT(8 4)  | POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  |

![](./ST_Within_10.png){align=center}

| geomA POINT | geomB POLYGON                       |
|-------------|-------------------------------------|
| POINT(8 4)  | POLYGON((1 1, 7 1, 7 7, 1 7, 1 1))  |

![](./ST_Within_11.png){align=center}

## See also

* [`ST_Contains`](../ST_Contains), [`ST_Overlaps`](../ST_Overlaps), [`ST_Touches`](../ST_Touches)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/predicates/ST_Within.java" target="_blank">Source code</a>
