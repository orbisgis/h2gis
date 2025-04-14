# ST_Accum

## Signatures

```sql
MULTIPOINT         ST_Accum(POINT geom);
MULTILINESTRING    ST_Accum(LINESTRING geom);
MULTIPOLYGON       ST_Accum(POLYGON geom);
GEOMETRYCOLLECTION ST_Accum(GEOMETRY geom);
```

## Description

This aggregate function constructs a `GEOMETRYCOLLECTION` from a column of mixed dimension Geometries.

If there is only `POINT`s in the column of Geometries, a `MULTIPOINT` is returned. Same process with `LINESTRING`s and `POLYGON`s.

## Examples

#### Case with same dimension geometries
```sql
CREATE TABLE input_table(geom GEOMETRY);
INSERT INTO input_table VALUES
    ('POINT(0 0)'),
    ('POINT(1 1)'),
    ('POINT(2 2)');
SELECT ST_Accum(geom) FROM input_table;
-- Answer: MULTIPOINT ((0 0), (1 1), (2 2))
```

#### Case with mixed dimension geometries
```sql
CREATE TABLE input_table(geom GEOMETRY);
INSERT INTO input_table VALUES
    ('POLYGON((9 0, 9 11, 10 11, 10 0, 9 0))'),
    ('POLYGON((1 1, 1 7, 7 7, 7 1, 1 1))'),
    ('POINT(1 1)'),
    ('POINT(2 2)');
SELECT ST_Accum(geom) FROM input_table;
-- Answer: GEOMETRYCOLLECTION(
--    POLYGON((9 0, 9 11, 10 11, 10 0, 9 0)),
--    POLYGON((1 1, 1 7, 7 7, 7 1, 1 1)),
--    POINT(1 1), POINT(2 2))
```

<img class="displayed" src="../ST_Accum.png"/>

## See also

* [`ST_Collect`](../ST_Collect), [`ST_Union`](../ST_Union), [`ST_Explode`](../ST_Explode)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/aggregate/ST_Accum.java" target="_blank">Source code</a>
