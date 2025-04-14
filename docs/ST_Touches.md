# ST_Touches

## Signature

```sql
BOOLEAN ST_Touches(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

Returns true if `geomA` touches `geomB`.

Touches means that:

* `geomA` and `geomB` have at least one point in common.
* The interiors of `geomA` and `geomB` do not intersect.

### Remarks

* `ST_Touches` can only be used to determine the relation between
  pairs listed here: `(POLYGON, POLYGON)`, `(POLYGON, LINESTRING)`,
  `(POLYGON, POINT)`, `(LINESTRING, LINESTRING)` and `(LINESTRING, POINT)`. 
The relation `(POINT, POINT)` is excluded.
* `GEOMETRYCOLLECTION`s are not taken into account.

```{include} sfs-1-2-1.md
```
{% include spatial_indice_warning.html %}

## Examples

### Cases where `ST_Touches` is true

```sql
SELECT ST_Touches(geomA, geomB) FROM input_table;
-- Answer:    TRUE
```

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | POLYGON((4 2, 7 2, 7 6, 4 6, 4 2))  |

![](./ST_Touches_1.png){align=center}

| geomA POLYGON                       | geomB LINESTRING      |
|-------------------------------------|-----------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | LINESTRING(2 5, 7 5)  |

![](./ST_Touches_2.png){align=center}

| geomA POLYGON                       | geomB POINT |
|-------------------------------------|-------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | POINT(4 3)  |

![](./ST_Touches_3.png){align=center}

| geomA LINESTRING           | geomB LINESTRING      |
|----------------------------|-----------------------|
| LINESTRING(2 1, 5 3, 2 6)  | LINESTRING(1 3, 3 5)  |

![](./ST_Touches_4.png){align=center}

| geomA LINESTRING           | geomB POINT |
|----------------------------|-------------|
| LINESTRING(2 1, 5 3, 2 6)  | POINT(2 6)  |

![](./ST_Touches_5.png){align=center}

| geomA POLYGON                       | geomB MULTIPOLYGON                                                      |
|-------------------------------------|-------------------------------------------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | MULTIPOLYGON(((4 2, 7 2, 7 6, 4 6, 4 2)), ((0 6, 1 6, 1 7, 0 7, 0 6)))  |

![](./ST_Touches_6.png){align=center}

| geomA POLYGON                       | geomB MULTILINESTRING                    |
|-------------------------------------|------------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | MULTILINESTRING((2 5, 7 5), (6 1, 6 4))  |

![](./ST_Touches_7.png){align=center}

| geomA POLYGON                       | geomB MULTIPOINT          |
|-------------------------------------|---------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | MULTIPOINT((4 3), (6 2))  |

![](./ST_Touches_8.png){align=center}

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | POLYGON((4 5, 7 5, 7 6, 4 6, 4 5))  |

![](./ST_Touches_9.png){align=center}

### Cases where `ST_Touches` is false

```sql
SELECT ST_Touches(geomA, geomB) FROM input_table;
-- Answer:    FALSE
```

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 5, 1 5, 1 1))  | POLYGON((3 4, 7 4, 7 6, 3 6, 3 4))  |

![](./ST_Touches_10.png){align=center}

## See also

* [`ST_Intersects`](../ST_Intersects)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/predicates/ST_Touches.java" target="_blank">Source code</a>
