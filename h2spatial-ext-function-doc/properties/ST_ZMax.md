### Name
`ST_ZMax` -- return the maximum z-value of the given geometry.

### Signature

```mysql
POINT ST_ZMax(Geometry geom);
```

### Description

Returns the maximum z-value of the given geometry.

### Examples

```mysql
SELECT ST_ZMax('LINESTRING(1 2 3, 4 5 6)'::Geometry);
-- Answer:    6.0
```

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
