# ST_Contains

## Signature

```sql
BOOLEAN ST_Contains(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

Returns true if `geomA` contains `geomB`.

```{include} sfs-1-2-1.md
```
{% include spatial_indice_warning.html %}

## Examples

##### Cases where `ST_Contains` is true

```sql
SELECT ST_Contains(geomA, geomB) FROM input_table;
-- Answer:    TRUE
```

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))  |

<img class="displayed" src="../ST_Contains_1.png"/>

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POLYGON((1 2, 6 2, 6 5, 1 5, 1 2))  |

<img class="displayed" src="../ST_Contains_4.png"/>

| geomA POLYGON                       | geomB LINESTRING      |
|-------------------------------------|-----------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | LINESTRING(2 6, 6 2)  |

<img class="displayed" src="../ST_Contains_2.png"/>

| geomA POLYGON                       | geomB LINESTRING           |
|-------------------------------------|----------------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | LINESTRING(1 2, 1 6, 5 2)  |

<img class="displayed" src="../ST_Contains_5.png"/>

| geomA POLYGON                       | geomB POINT |
|-------------------------------------|-------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POINT(4 4)  |

<img class="displayed" src="../ST_Contains_3.png"/>

| geomA LINESTRING           | geomB LINESTRING      |
|----------------------------|-----------------------|
| LINESTRING(2 1, 5 3, 2 6)  | LINESTRING(3 5, 5 3)  |

<img class="displayed" src="../ST_Contains_10.png"/>

| geomA LINESTRING           | geomB POINT |
|----------------------------|-------------|
| LINESTRING(2 1, 5 3, 2 6)  | POINT(4 4)  |

<img class="displayed" src="../ST_Contains_11.png"/>

##### Cases where `ST_Contains` is false

```sql
SELECT ST_Contains(geomA, geomB) FROM input_table;
-- Answer:    FALSE
```

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POLYGON((0 2, 5 2, 5 5, 0 5, 0 2))  |

<img class="displayed" src="../ST_Contains_7.png"/>

| geomA POLYGON                       | geomB LINESTRING      |
|-------------------------------------|-----------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | LINESTRING(2 6, 0 8)  |

<img class="displayed" src="../ST_Contains_8.png"/>

| geomA POLYGON                       | geomB LINESTRING      |
|-------------------------------------|-----------------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | LINESTRING(1 2, 1 6)  |

<img class="displayed" src="../ST_Contains_12.png"/>

| geomA POLYGON                       | geomB POINT |
|-------------------------------------|-------------|
| POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))  | POINT(8 4)  |

<img class="displayed" src="../ST_Contains_6.png"/>

| geomA POLYGON                       | geomB POINT |
|-------------------------------------|-------------|
| POLYGON((1 1, 7 1, 7 7, 1 7, 1 1))  | POINT(8 4)  |

<img class="displayed" src="../ST_Contains_9.png"/>

## See also

* [`ST_Intersects`](../ST_Intersects), [`ST_Touches`](../ST_Touches), [`ST_Overlaps`](../ST_Overlaps)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/predicates/ST_Contains.java" target="_blank">Source code</a>
