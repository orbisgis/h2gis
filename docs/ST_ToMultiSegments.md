# ST_ToMultiSegments

## Signatures

```sql
MULTILINESTRING ST_ToMultiSegments(GEOMETRY geom);
MULTILINESTRING ST_ToMultiSegments(GEOMETRYCOLLECTION geom);
```

## Description

Converts `geom` into a set of distinct segments stored in a `MULTILINESTRING`.
Returns `MULTILINESTRING EMPTY` for Geometries of dimension 0.

## Examples

```sql
SELECT ST_ToMultiSegments('LINESTRING(5 4, 1 1, 3 4, 4 5)');
-- Answer: MULTILINESTRING((5 4, 1 1), (1 1, 3 4), (3 4, 4 5))
```

![](./ST_ToMultiSegments1.png){align=center}

```sql
SELECT ST_ToMultiSegments(
    'MULTILINESTRING((1 4 3, 15 7 9, 16 17 22),
                     (0 0 0, 1 0 0, 1 2 0, 0 2 1))');
-- Answer: MULTILINESTRING((1 4, 15 7), (15 7, 16 17),
--                          (0 0, 1 0), (1 0, 1 2), (1 2, 0 2))

SELECT ST_ToMultiSegments(
    'POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
              (1 1, 2 1, 2 5, 1 5, 1 1))');
-- Answer: MULTILINESTRING((0 0, 10 0), (10 0, 10 6), (10 6, 0 6),
--                          (0 6, 0 0), (1 1, 2 1), (2 1, 2 5),
--                          (2 5, 1 5), (1 5, 1 1))
```

![](./ST_ToMultiSegments2.png){align=center}

```sql
SELECT ST_ToMultiSegments(
    'GEOMETRYCOLLECTION(
       POLYGON((0 0, 10 0, 10 5, 0 5, 0 0),
                (1 1, 2 1, 2 4, 1 4, 1 1),
                (7 1, 8 1, 8 3, 7 3, 7 1)),
       POINT(2 3),
       LINESTRING(8 7, 9 5, 11 3))');
-- Answer:MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5),
--                         (0 5, 0 0), (1 1, 2 1), (2 1, 2 4),
--                         (2 4, 1 4), (1 4, 1 1), (7 1, 8 1),
--                         (8 1, 8 3), (8 3, 7 3), (7 3, 7 1),
--                         (8 7, 9 5), (9 5, 11 3))

SELECT ST_ToMultiSegments('POINT(5 5)');
-- Answer: MULTILINESTRING EMPTY
```

### Comparison with [`ST_ToMultiLine`](../ST_ToMultiLine)

```sql
CREATE TABLE input(poly POLYGON);
INSERT INTO input VALUES (
    'POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
              (1 1, 2 1, 2 5, 1 5, 1 1),
              (7 1, 8 1, 8 3, 7 3, 7 1))');
SELECT ST_ToMultiSegments(poly) SEG,
       ST_ToMultiLine(poly) LINE FROM input;
```

Answer:
|              SEG             |               LINE              |
|------------------------------|---------------------------------|
|MULTILINESTRING(<br>(0 0, 10 0), (10 0, 10 6), (10 6, 0 6), (0 6, 0 0),<br> (1 1, 2 1), (2 1, 2 5), (2 5, 1 5), (1 5, 1 1),<br> (7 1, 8 1), (8 1, 8 3), (8 3, 7 3), (7 3, 7 1)) |MULTILINESTRING ((0 0, 10 0, 10 6, 0 6, 0 0),<br> (1 1, 2 1, 2 5, 1 5, 1 1),<br> (7 1, 8 1, 8 3, 7 3, 7 1)) |

![](./ST_ToMultiSegments3.png){align=center}

## See also

* [`ST_ToMultiLine`](../ST_ToMultiLine)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_ToMultiSegments.java" target="_blank">Source code</a>
