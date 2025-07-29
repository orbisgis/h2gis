# ST_LocateAlong

## Signature

```sql
MULTIPOINT ST_LocateAlong(GEOMETRY geom,
                          DOUBLE segmentLengthFraction,
                          DOUBLE offsetDistance);
```

## Description

Places points along the line segments composing `geom` at a distance of
`segmentLengthFraction` along the segment and at an offset distance of
`offsetDistance`. Returns them as a `MULTIPOINT`.

:::{note}
**What about orientation?**

Line segment orientation is determined by the order of the coordinates. A positive offset places the point to the left of the segment; a negative offset to the right.
:::

:::{Warning}
**Only exterior rings are supported for `POLYGON`s**
:::

## Examples

```sql
SELECT ST_LocateAlong('LINESTRING(1 1, 5 4)', 0.5, 2);
-- Answer: MULTIPOINT((1.8 4.1))
```

![](./ST_LocateAlong_0.png){align=center}

```sql
SELECT ST_LocateAlong('LINESTRING(1 1, 5 1, 5 3)', 0.5, 1);
-- Answer: MULTIPOINT((3 2), (4 2))
```

![](./ST_LocateAlong_1.png){align=center}

```sql
SELECT ST_LocateAlong('POLYGON((1 1, 4 1, 4 3, 1 3, 1 1))', 0.5, -1);
-- Answer: MULTIPOINT((2.5 0), (5 2), (2.5 4), (0 2))
```

![](./ST_LocateAlong_2.png){align=center}

```sql
SELECT ST_LocateAlong('GEOMETRYCOLLECTION(
                           LINESTRING(1 4, 5 4, 5 2),
                           POLYGON((1 1, 4 1, 4 3, 1 3, 1 1)))',
                      2, 1);
-- Answer: MULTIPOINT((2 -1), (-2 2), (6 0), (9 5), (7 2), (3 5))
```

![](./ST_LocateAlong_3.png){align=center}

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/distance/ST_LocateAlong.java" target="_blank">Source code</a>
