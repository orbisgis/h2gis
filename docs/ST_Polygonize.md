# ST_Polygonize

## Signature

```sql
MULTIPOLYGON ST_Polygonize(GEOMETRY geom);
```

## Description

Creates a `MULTIPOLYGON` containing all possible `POLYGON`s formed from `geom`.

:::{note}
**Returns `NULL` if the endpoints of `geom` are not properly joined or `geom` cannot be "polygonized" (e.g., `POINT`s)**
:::

## Examples

```sql
SELECT ST_Polygonize('LINESTRING(1 2, 2 4, 4 4, 5 2, 1 2)');
-- Answer: MULTIPOLYGON(((1 2, 2 4, 4 4, 5 2, 1 2)))
```

![](./ST_Polygonize_2.png){align=center}

```sql
SELECT ST_Polygonize('MULTILINESTRING((1 2, 2 4, 5 2),
                                      (5 2, 2 1, 1 2))');
-- Answer: MULTIPOLYGON(((1 2, 2 4, 5 2, 2 1, 1 2)))
```

![](./ST_Polygonize_3.png){align=center}

```sql
-- ST_Polygonize of a POLYGON is the same POLYGON converted to a
-- MULTIPOLYGON:
SELECT ST_Polygonize('POLYGON((2 2, 2 4, 5 4, 5 2, 2 2))');
-- Answer: MULTIPOLYGON((2 2, 2 4, 5 4, 5 2, 2 2))
```

This example shows that `ST_Polygonize` is "greedy" in the sense that it will construct as many POLYGONs as possible. Here it finds only one:
```sql
SELECT ST_Polygonize(ST_Union('MULTILINESTRING((1 2, 2 4, 5 2),
                                               (1 4, 4 1, 4 4))'));
-- Answer: MULTIPOLYGON(((1.6666666666666667 3.3333333333333335,
--                        2 4, 4 2.6666666666666665, 4 1,
--                        1.6666666666666667 3.3333333333333335)))
```

![](./ST_Polygonize_4.png){align=center}

Here we do the same example as before but close the LINESTRINGs, so that three polygons are produced:
```sql
SELECT ST_Polygonize(
            ST_Union('MULTILINESTRING((1 2, 2 4, 5 2),
                                      (1 2, 1 4, 4 1, 4 4, 5 2))'));
Answer: MULTIPOLYGON(((4 2.6666666666666665, 4 1,
                        1.6666666666666667 3.3333333333333335,
                        2 4, 4 2.6666666666666665)),
                      ((1.6666666666666667 3.3333333333333335,
                        1 2, 1 4,
                        1.6666666666666667 3.3333333333333335)),
                      ((4 2.6666666666666665,
                        4 4, 5 2,
                        4 2.6666666666666665)))
```

### Non-examples

Returns NULL for Geometries which cannot be "polygonized":
```sql
SELECT ST_Polygonize('POINT(1 2)');
-- Answer: NULL
```

In the following three examples, the endpoints are not properly joined:
```sql
SELECT ST_Polygonize('MULTILINESTRING((1 2, 2 4, 5 2),
                                      (1 4, 4 1, 4 4))')
-- Answer: NULL
```

```sql
SELECT ST_Polygonize('MULTILINESTRING((1 2, 2 4, 4 4, 5 2),
                                      (5 2, 2 1, 2 4, 1 5))');
-- Answer: NULL
```
```sql
SELECT ST_Polygonize('LINESTRING(1 2, 2 4, 4 4, 5 2, 2 2)');
-- Answer: NULL
```

![](./ST_Polygonize_1.png){align=center}

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/topology/ST_Polygonize.java" target="_blank">Source code</a>
* JTS [Polygonizer#getPolygons][jts]

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/operation/polygonize/Polygonizer.html#getPolygons()
