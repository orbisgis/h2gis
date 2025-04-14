# ST_CompactnessRatio

## Signature

```sql
DOUBLE ST_CompactnessRatio(POLYGON poly);
```

## Description

`ST_CompactnessRatio` computes the perimeter of a circle whose area is equal to the given `POLYGON`'s area, and returns the ratio of this computed perimeter to the given `POLYGON`'s perimeter.

Equivalent definition: `ST_CompactnessRatio` returns the square root of the `POLYGON`'s area divided by the area of the circle with circumference equal to the `POLYGON`'s perimeter.

Note: This uses the 2D perimeter/area of the `POLYGON`.
This function accepts only `POLYGON`s.

## Examples

```sql
CREATE TABLE input_table(geom GEOMETRY);
INSERT INTO input_table VALUES
    ('POLYGON((9 0, 9 11, 10 11, 10 0, 9 0))'),
    ('POLYGON((1 1, 1 7, 7 7, 7 1, 1 1))');
SELECT ST_CompactnessRatio(geom) ratio FROM input_table;
-- Answer:
--    |         RATIO       |
--    |---------------------|
--    | 0.48988036513951067 |
--    |  0.886226925452758  |
```

*Note*: In this example, both geometries have a perimeter equal to 24.

<img class="displayed" src="../ST_CompactnessRatio.png"/>

```sql
SELECT ST_CompactnessRatio(ST_Buffer(('POINT(1 2)'), 10));
-- Answer: 0.9983912919723259
--    Note: A buffer is a polygonal approximation to a circle.
--    ST_Buffer uses 32 line segments. That explains why the
--    compactness ratio is slightly less than 1.

SELECT ST_CompactnessRatio(ST_MakeEllipse('POINT(1 2)', 10, 10));
-- Answer: 0.9998354822360185
--    Note: ST_MakeEllipse approximates using 100 line segments.
--    So the approximation is more precise, explaining why this
--    result is closer to 1 than the result from ST_Buffer.

SELECT ST_CompactnessRatio(
    'POLYGON((4 12, 1 6, 6 3, 15 2, 17 5, 16 10, 9 14, 4 12),
             (7 9, 6 7, 10 6, 10 8, 7 9))');
-- Answer: 0.7142366622175312

SELECT ST_CompactnessRatio(
    'POLYGON((0 0 0, 3 0 0, 3 2 0, 0 2 1, 0 0 0))');
-- Answer: 0.8683215054699213

SELECT ST_CompactnessRatio('POINT(1 2)');
-- Answer: NULL
```

## See also

* [`ST_Buffer`](../ST_Buffer), [`ST_MakeEllipse`](../ST_MakeEllipse)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_CompactnessRatio.java" target="_blank">Source code</a>
