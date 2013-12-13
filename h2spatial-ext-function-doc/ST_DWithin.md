### Name

`ST_DWithin` -- return true if the geometries are within the specified distance
of one another.

### Signature

```mysql
boolean ST_DWithin(Geometry geomA, Geometry geomB, double distance);
```

### Description

Returns true if `geomA` is within `distance` of `geomB`.

### Examples


| geomA Polygon | geomB Polygon |
| --------- |
| POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0)) |
| POLYGON ((12 0, 14 0, 14 6, 12 6, 12 0)) |

```mysql
SELECT ST_DWithin(geomA, geomB, 2.0) FROM input_table;
```
Answer:    `true`
```mysql
SELECT ST_DWithin(geomA, geomB, 1.0) FROM input_table;
```
Answer:    `false`
```mysql
SELECT ST_DWithin(geomA, geomB, -1.0) FROM input_table;
```
Answer:    `false`
```mysql
SELECT ST_DWithin(geomA, geomB, 3.0) FROM input_table;
```
Answer:    `true`
```mysql
SELECT ST_DWithin(geomA, geomA, -1.0) FROM input_table;
```
Answer:    `false`
```mysql
SELECT ST_DWithin(geomA, geomA, 0.0) FROM input_table;
```
Answer:    `true`
```mysql
SELECT ST_DWithin(geomA, geomA, 5000.0) FROM input_table;
```
Answer:    `true`

##### History

* Added: [#26](https://github.com/irstv/H2GIS/pull/26)
