### Name

`ST_IsValid` -- return true if the given geometry is valid.

### Signature

```mysql
boolean ST_IsValid(Geometry geom);
```

### Description

Returns true if `geom` is valid.

### Examples

```mysql
SELECT ST_IsValid('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'::Geometry) FROM input_table;
```
Answer:    `true`
```mysql
SELECT ST_IsValid('POLYGON ((0 0, 10 0, 10 5, 10 -5, 0 0))'::Geometry) FROM input_table;
```
Answer:    `false`

##### History

* Added: [#26](https://github.com/irstv/H2GIS/pull/26)
