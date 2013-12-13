### Name
`ST_ZMin` -- return the minimum z-value of the given geometry.

### Signature

```mysql
POINT ST_ZMin(Geometry geom);
```

### Description

Returns the minimum z-value of the given geometry.

### Examples

```mysql
SELECT ST_ZMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    3.0
```

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
