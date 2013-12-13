### Name
`ST_IsRectangle` -- return true if the given geometry is a rectangle.

### Signature

```mysql
boolean ST_IsRectangle(Geometry geom);
```

### Description

Returns true if `geom` is a rectangle.

### Examples

```mysql
SELECT ST_IsRectangle('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))'::Geometry);
-- Answer:    true

SELECT ST_IsRectangle('POLYGON ((0 0, 10 0, 10 7, 0 5, 0 0))'::Geometry);
-- Answer:    false
```

##### History

* Added: [#26](https://github.com/irstv/H2GIS/pull/26)
