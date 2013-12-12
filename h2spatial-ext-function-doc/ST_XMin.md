### Name
`ST_XMin` -- return the minimum x-value of the given geometry.

### Signature

```mysql
POINT ST_XMin(Geometry geom);
```

### Description

Returns the minimum x-value of the given geometry.

### Examples

```mysql
SELECT ST_XMin('LINESTRING(1 2 3, 4 5 6)'::Geometry);
```
Answer:    `1.0`

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
