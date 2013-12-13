### Name
`ST_YMax` -- return the maximum y-value of the given geometry.

### Signature

```mysql
POINT ST_YMax(Geometry geom);
```

### Description

Returns the maximum y-value of the given geometry.

### Examples

```mysql
SELECT ST_YMax('LINESTRING(1 2 3, 4 5 6)'::Geometry);
```
Answer:    `5.0`

##### History

* Added: [#28](https://github.com/irstv/H2GIS/pull/28)
