### Name
`ST_Covers` -- return true if no point in geometry B is outside geometry A.

### Signature

```mysql
boolean ST_Covers(Geometry geomA, Geometry geomB);
```

### Description

Returns true if no point in `geomB` is outside `geomA`.

### Examples

| smallc Polygon | bigc Polygon |
| ----|---- |
| `ST_Buffer(ST_GeomFromText('POINT(1 2)'), 10)` | `ST_Buffer(ST_GeomFromText('POINT(1 2)'), 20))` |

```mysql
SELECT ST_Covers(smallc, smallc) FROM input_table;
-- Answer:    true

SELECT ST_Covers(smallc, bigc) FROM input_table;
-- Answer:    false

SELECT ST_Covers(bigc, smallc) FROM input_table;
-- Answer:    true

SELECT ST_Covers(bigc, ST_ExteriorRing(bigc)) FROM input_table;
-- Answer:    true

SELECT ST_Contains(bigc, ST_ExteriorRing(bigc)) FROM input_table;
-- Answer:    false
```

##### History

* Added: [#26](https://github.com/irstv/H2GIS/pull/26)
