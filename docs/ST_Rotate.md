# ST_Rotate

## Signatures

```sql
GEOMETRY ST_Rotate(GEOMETRY geom, DOUBLE angle);
GEOMETRY ST_Rotate(GEOMETRY geom, DOUBLE angle, POINT origin);
GEOMETRY ST_Rotate(GEOMETRY geom, DOUBLE angle, DOUBLE x, DOUBLE y);
```

## Description

Rotates `geom` counter-clockwise by `angle` (in radians) about the point
`origin` (or about the point specified by coordinates `x` and `y`).  If no
point is specified, `geom` is rotated about its center (the center of its
internal envelope).

## Examples

```sql
SELECT ST_Rotate('LINESTRING(1 3, 1 1, 2 1)', pi());
-- Answer:    LINESTRING(2 1, 2 3, 1 3)
```

![](./ST_Rotate.png){align=center}


```sql
SELECT ST_Rotate('LINESTRING(1 3, 1 1, 2 1)', pi() / 3);
-- Answer: LINESTRING(0.3839745962155607 2.0669872981077813,
--                    2.1160254037844384 1.0669872981077806,
--                    2.6160254037844384 1.933012701892219)

SELECT ST_Rotate('LINESTRING(1 3, 1 1, 2 1)', -pi()/2, ST_GeomFromText('POINT(2 1)'));
-- Answer:    LINESTRING(4 1, 2 2, 2 1)

SELECT ST_Rotate('LINESTRING(1 3, 1 1, 2 1)', pi()/2, 1.0, 1.0);
-- Answer:    LINESTRING(-1 1, 1 1, 1 2)
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/affine_transformations/ST_Rotate.java" target="_blank">Source code</a>
