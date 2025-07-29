# ST_Force4D

## Signature

```sql
GEOMETRY ST_Force4D(GEOMETRY geom);
GEOMETRY ST_Force4D(GEOMETRY geom, DOUBLE zValue, DOUBLE mValue);
```

## Description

Forces a geometry `geom` to be in `XYZM` mode.

If `geom` has no `Z` or `M` component, then a `Z` and `M` value are tacked on.

User can set :
* a default `Z` value (`zValue`). If not specified, `zValue` is set to zero.
* a default `M` value (`mValue`). If not specified, `mValue` is set to zero.

If `geom` has already a `Z` or `M` values, then they are untouched and just completed with the missing one. 

Already `XYZM` geometries are returned untouched.

## Examples

### No effect on `XYZM` geometries

```sql
SELECT ST_Force4D('POINT ZM(-10 10 2 6)');
-- Answer:         POINT ZM(-10 10 2 6)
```

### Adding `M` dimension
```sql
SELECT ST_Force4D('POINT(-10 10)');
-- Answer:         POINT ZM(-10 10 0 0)
```

```sql
SELECT ST_Force4D('LINESTRING(-10 10, 10 10)');
-- Answer:         LINESTRING ZM (-10 10 0 0, 10 10 0 0)
```

### Specifying `mValue`

```sql
SELECT ST_Force4D('POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))', 5, 3);
-- Answer:         POLYGON ZM ((2 2 5 3, 10 0 5 3, 10 5 5 3, 0 5 5 3, 2 2 5 3))
```

### Completing existing `Z` or `M` dimensions and adding the `ZM` one

```sql
SELECT ST_Force4D('POINT Z(-10 10 12)',3, 2);
-- Answer:         POINT ZM (-10 10 12 2)

SELECT ST_Force4D('LINESTRING Z(-10 10 4, 10 10 6)', 2, 3);
-- Answer:         LINESTRING ZM (-10 10 4 3, 10 10 6 3)

SELECT ST_Force4D('POLYGON M((2 2 4, 10 0 3, 10 5 2, 0 5 1, 2 2 4))', 2, 5);
-- Answer:         POLYGON ZM ((2 2 2 4, 10 0 2 3, 10 5 2 2, 0 5 2 1, 2 2 2 4))
```

## See also

* [`ST_Force2D`](../ST_Force2D), [`ST_Force3D`](../ST_Force3D), [`ST_Force3DM`](../ST_Force3DM), [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_Force4D.java" target="_blank">Source code</a>
