### Name
`ST_XMax` -- return the maximum x-value of the given geometry.

### Signatures

```mysql
POINT ST_XMax(Geometry geom);
```

### Description

Returns the maximum x-value of the given geometry.

### Examples

```mysql
SELECT ST_XMax('LINESTRING(1 2 3, 4 5 6)'::Geometry);
```
Answer:    `4.0`

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
