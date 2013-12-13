### Name
`ST_3DLength` -- return the 3D length or the 3D perimeter of the given
geometry.
 
### Signature

```mysql
double ST_3DLength(Geometry geom);
```

### Description

Returns the 3D length (of a `LINESTRING`) or the 3D perimeter (of a `POLYGON`).
In the case of a 2D geometry, `ST_3DLength` returns the same value as
`ST_Length`.

### Examples

| geom Geometry |
| --------- |
| `LINESTRING(1 4, 15 7, 16 17)` |
| `LINESTRING(1 4 3, 15 7 9, 16 17 22)` |
| `MULTILINESTRING((1 4 3, 15 7 9, 16 17 22), (0 0 0, 1 0 0, 1 2 0, 0 2 1))` |
| `POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))` |
| `POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))` |
| `MULTIPOLYGON(((0 0 0, 3 2 0, 3 2 2, 0 0 2, 0 0 0), (-1 1 0, -1 3 0, -1 3 4, -1 1 4, -1 1 0)))` |
| `GEOMETRYCOLLECTION(LINESTRING(1 4 3, 15 7 9, 16 17 22), POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))` |

```mysql
SELECT ST_3DLength(geom) FROM input_table;
```

| `ST_3DLength(geom)` |
| ---- |
| 24.367696684397245 |
| 31.955851421415005 |
| 36.3700649837881 |
| 6.0 |
| 9.048627177541054 |
| 23.21110255092798 |
| 41.004478598956055 |

*Note*: The exact mathematical values are the following:

| `ST_3DLength(geom)` |
| ---- |
| `SQRT(205) + SQRT(101)` |
| `SQRT(241) + SQRT(270)` |
| `SQRT(241) + SQRT(270) + 3 + SQRT(2)` |
| `6` |
| `SQRT(2) + 2 * SQRT(5) + SQRT(10)` |
| `16 + 2 * SQRT(13)` |
| `SQRT(241) + SQRT(270) + SQRT(2) + 2 * SQRT(5) + SQRT(10)` |

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
