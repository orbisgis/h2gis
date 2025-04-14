# ST_Drape

## Signature

```sql
GEOMETRY ST_Drape(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

This function drapes an input geometry (`geomA`) to a set of triangles (`geomB`).

Notes : 

* The supported input geometry types are `POINT`, `MULTIPOINT`, `LINESTRING`, `MULTILINESTRING`, `POLYGON` and `MULTIPOLYGON`
* In case of 1 or 2 dimension, the input geometry (`geomA`) is intersected with the triangles to perform a full draping.
* If a `POINT` lies on two triangles, the `z` value of the first triangle is kept.

## Examples

### Case between a `LINESTRING` and a `POLYGON`

```sql
SELECT ST_Drape('LINESTRING(1 2, 5 2)',
                'POLYGON((1 1 0, 4 1 0, 4 4 5, 1 1 0))');
-- Answer: LINESTRING(1 2, 2 2 2, 4 2 2, 5 2)
```

<img class="displayed" src="../ST_Drape_1.png"/>

### Case between a `LINESTRING` and a `MULTIPOLYGON`

```sql
SELECT ST_Drape('LINESTRING(1 2, 5 2)',
                'MULTIPOLYGON(((1 1 0, 4 1 0, 4 4 5, 1 1 0))
                              ((4 1 0, 4 4 5, 5 4 5, 4 1 0))');
-- Answer: LINESTRING(1 2, 2 2 1.66, 4 2 1.66, 4.33 2 1.66, 5 2)
```

<img class="displayed" src="../ST_Drape_2.png"/>

### Case between two `POLYGONS`

```sql
SELECT ST_Drape('POLYGON((1 2, 5 2, 2 5, 1 2))',
                'POLYGON((1 1 0, 4 1 0, 4 4 5, 1 1 0))');
-- Answer: POLYGON((1 2, 2 2 1.66, 4 2 1.66, 5 2, 
--                  4 3 3.33, 3.5 3.5 4.16, 2 5, 1 2))
```

<img class="displayed" src="../ST_Drape_3.png"/>

### Case between two `POLYGONS` *(input one with hole)*

```sql
SELECT ST_Drape('POLYGON ((1 2, 5 2, 2 5, 1 2),(2 2.5, 2 3.5, 3.5 2.5, 2 2.5))',
                'POLYGON((1 1 0, 4 1 0, 4 4 5, 1 1 0))');
-- Answer: POLYGON((1 2, 2 2 1.66, 4 2 1.66, 5 2, 4 3 3.33, 3.5 3.5 4.16, 2 5, 1 2), 
--                 (2 2.5, 2 3.5, 2.9 2.9 3.16, 3.5 2.5 2.5, 2.5 2.5 2.5, 2 2.5))
```

<img class="displayed" src="../ST_Drape_4.png"/>

### Case between a `MULTIPOINT` and a `POLYGON`

```sql
SELECT ST_Drape('MULTIPOINT((1 2), (2 2), (3 2), (4 3))',
                'POLYGON((1 1 0, 4 1 0, 4 4 5, 1 1 0))');
-- Answer: MULTIPOINT((1 2), (2 2 1.66), (3 2 1.66), (4 3 3.33))
```

<img class="displayed" src="../ST_Drape_5.png"/>

### Case between a `MULTILINESTRING` and a `POLYGON`

```sql
SELECT ST_Drape('MULTILINESTRING((1 2, 3 2),(2 4, 5 2))',
                'POLYGON((1 1 0, 4 1 0, 4 4 5, 1 1 0))');
-- Answer: MULTILINESTRING((1 2, 2 2 1.66, 3 2 1.66),
--                         (2 4, 3.2 3.2 3.66, 4 2.66 2.77, 5 2))
```

<img class="displayed" src="../ST_Drape_6.png"/>

### Case between a `MULTIPOLYGON` and a `POLYGON`

```sql
SELECT ST_Drape('MULTIPOLYGON(((1 2, 3 2, 2 3, 1 2)),
                              ((2 4, 5 2, 5 5, 2 4)))',
                'POLYGON((1 1 0, 4 1 0, 4 4 5, 1 1 0))');
-- Answer: MULTIPOLYGON(((1 2, 2 2 1.66, 3 2 1.66, 2.5 2.5 2.5, 2 3, 1 2)), 
--                      ((2 4, 3.2 3.2 3.66, 4 2.66 2.77, 5 2, 5 5, 2 4)))
```

<img class="displayed" src="../ST_Drape_7.png"/>

## See also

* [`ST_Z`](../ST_Z), [`ST_ZMax`](../ST_ZMax), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/topography/ST_Drape.java" target="_blank">Source code</a>
