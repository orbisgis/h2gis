### Name
`ST_YMin` -- return the minimum y-value of the given geometry.

### Signature

```mysql
POINT ST_YMin(Geometry geom);
```

### Description

Returns the minimum y-value of the given geometry.

### Examples

```mysql
SELECT ST_YMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    2.0
```

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
