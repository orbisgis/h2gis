# ST_Intersection

## Signature

```sql
GEOMETRY ST_Intersection(GEOMETRY geomA, GEOMETRY geomB)
```

## Description

Computes the intersection between `geomA` and `geomB`.

`geomA` and `geomB` can be `POINT`s, `LINESTRING`s, `POLYGON`s or `GEOMETRYCOLLECTION`s

```{include} sfs-1-2-1.md
```

## Examples

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 7 1, 7 6, 1 6, 1 1))  | POLYGON((3 2, 8 2, 8 8, 3 8, 3 2))  |

```sql
SELECT ST_Intersection(geomA, geomB) FROM input_table;
-- Answer:    POLYGON((3 6, 7 6, 7 2, 3 2, 3 6))
```

<img class="displayed" src="../ST_Intersection_1.png"/>

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 6, 1 6, 1 1))  | POLYGON((4 2, 8 2, 8 8, 4 8, 4 2))  |

```sql
SELECT ST_Intersection(geomA, geomB) FROM input_table;
-- Answer:    LINESTRING(4 2, 4 6)
```

<img class="displayed" src="../ST_Intersection_2.png"/>

| geomA POLYGON                       | geomB POLYGON                       |
|-------------------------------------|-------------------------------------|
| POLYGON((1 1, 4 1, 4 6, 1 6, 1 1))  | POLYGON((4 6, 8 6, 8 8, 4 8, 4 6))  |

```sql
SELECT ST_Intersection(geomA, geomB) FROM input_table;
-- Answer:    POINT(4 6)
```

<img class="displayed" src="../ST_Intersection_6.png"/>

| geomA POLYGON                       | geomB LINESTRING      |
|-------------------------------------|-----------------------|
| POLYGON((1 1, 7 1, 7 6, 1 6, 1 1))  | LINESTRING(2 8, 8 2)  |

```sql
SELECT ST_Intersection(geomA, geomB) FROM input_table;
-- Answer:    LINESTRING(4 6, 7 3)
```

<img class="displayed" src="../ST_Intersection_3.png"/>

| geomA LINESTRING      | geomB LINESTRING      |
|-----------------------|-----------------------|
| LINESTRING(2 2, 6 6)  | LINESTRING(2 8, 8 2)  |

```sql
SELECT ST_Intersection(geomA, geomB) FROM input_table;
-- Answer:    POINT(5 5)
```

<img class="displayed" src="../ST_Intersection_4.png"/>

| geomA POLYGON                       | geomB POINT |
|-------------------------------------|-------------|
| POLYGON((1 1, 7 1, 7 6, 1 6, 1 1))  | POINT(3 5)  |

```sql
SELECT ST_Intersection(geomA, geomB) FROM input_table;
-- Answer:    POINT(3 5)
```

<img class="displayed" src="../ST_Intersection_5.png"/>

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/operators/ST_Intersection.java" target="_blank">Source code</a>
