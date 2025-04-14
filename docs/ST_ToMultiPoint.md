# ST_ToMultiPoint

## Signature

```sql
MULTIPOINT ST_ToMultiPoint(GEOMETRY geom);
MULTIPOINT ST_ToMultiPoint(GEOMETRYCOLLECTION geom);
```

## Description

Constructs a `MULTIPOINT` from `geom`'s coordinates.

## Examples

```sql
SELECT ST_ToMultiPoint('POINT(5 5)');
-- Answer: MULTIPOINT((5 5))

SELECT ST_ToMultiPoint('MULTIPOINT(5 5, 1 2, 3 4, 99 3)');
-- Answer: MULTIPOINT((5 5), (1 2), (3 4), (99 3))

SELECT ST_ToMultiPoint('LINESTRING(5 5, 1 2, 3 4, 1 5)');
-- Answer: MULTIPOINT((5 5), (1 2), (3 4), (1 5))
```

<img class="displayed" src="../ST_ToMultiPoint1.png"/>

```sql
SELECT ST_ToMultiPoint('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))');
-- Answer: MULTIPOINT((0 0), (10 0), (10 5), (0 5), (0 0))

SELECT ST_ToMultiPoint(
    'MULTIPOLYGON(((28 26, 28 0, 84 0, 84 42, 28 26),
                   (52 18, 66 23, 73 9, 48 6, 52 18)),
                  ((59 18, 67 18, 67 13, 59 13, 59 18)))');
-- Answer: MULTIPOINT((28 26), (28 0), (84 0), (84 42), (28 26),
--                     (52 18), (66 23), (73 9), (48 6), (52 18),
--                     (59 18), (67 18), (67 13), (59 13), (59 18))

SELECT ST_ToMultiPoint(
    'GEOMETRYCOLLECTION(
       POLYGON((0 0, 10 0, 10 6, 0 6, 0 0)),
       LINESTRING(5 5, 1 2, 3 4, 1 5))');
-- Answer: MULTIPOINT((0 0), (10 0), (10 6), (0 6), (0 0),
--                     (5 5), (1 2), (3 4), (99 3))
```

<img class="displayed" src="../ST_ToMultiPoint2.png"/>

### Aggregate form

```sql
CREATE TABLE input_table(line LINESTRING);
INSERT INTO input_table VALUES
    ('LINESTRING(5 5, 1 2, 3 4, 0 3))'),
    ('LINESTRING(0 0, 1 -2, 3 1))'),
    ('LINESTRING(0 1, 2 2, 3 6))');
SELECT ST_ToMultiPoint(ST_Accum(line)) FROM input_table;
-- Answer: MULTIPOINT((5 5), (1 2), (3 4), (0 3), (0 0),
--                     (1 -2), (3 1), (0 1), (2 2), (3 6))
```

## See also

* [`ST_Accum`](../ST_Accum)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_ToMultiPoint.java" target="_blank">Source code</a>
