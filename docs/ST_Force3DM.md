# ST_Force3DM

## Signature

```sql
GEOMETRY ST_Force3DM(GEOMETRY geom);
GEOMETRY ST_Force3DM(GEOMETRY geom, DOUBLE mValue);
```

## Description

Forces a geometry `geom` to be in `XYM` mode.

If `geom` has no `M` component, then a `M` value is tacked on.

User can set a default `M` value (`mValue`). If not specified, `mValue` is set to zero.

If `geom` has already a `Z` value, then it is removed. 

Already `XYM` geometries are returned untouched.

## Examples

### No effect on `XYM` geometries

```sql
SELECT ST_Force3DM('POINT M(-10 10 6)');
-- Answer:          POINT M(-10 10 6)
```

### Adding `M` dimension
```sql
SELECT ST_Force3DM('POINT(-10 10)');
-- Answer:          POINT M(-10 10 0)
```

```sql
SELECT ST_Force3DM('LINESTRING(-10 10, 10 10)');
-- Answer:          LINESTRING M(-10 10 0, 10 10 0)
```

```sql
SELECT ST_Force3DM('GEOMETRYCOLLECTION(
                        POINT(1 1), 
                        LINESTRING(-10 10, 10 10), 
                        POLYGON((2 2, 10 0, 10 5, 0 5, 2 2)))');
-- Answer:          GEOMETRYCOLLECTION M (
--                      POINT M (1 1 0), 
--                      LINESTRING M (-10 10 0, 10 10 0), 
--                      POLYGON M ((2 2 0, 10 0 0, 10 5 0, 0 5 0, 2 2 0)))
```

### Specifying `mValue`

```sql
SELECT ST_Force3DM('POLYGON((2 2, 10 0, 10 5, 0 5, 2 2))', 5);
-- Answer:          POLYGON M((2 2 5, 10 0 5, 10 5 5, 0 5 5, 2 2 5))
```

```sql
SELECT ST_Force3DM('MULTIPOINT((2 2), (10 0))', 5);
-- Answer:          MULTIPOINT M ((2 2 5), (10 0 5))
```

### Removing `Z` dimension and adding the `M` one

```sql
SELECT ST_Force3DM('POINT Z(-10 10 12)',10);
-- Answer:          POINT M(-10 10 10)

SELECT ST_Force3DM('LINESTRING Z(-10 10 4, 10 10 6)', 3);
-- Answer:          LINESTRING M(-10 10 3, 10 10 3)

SELECT ST_Force3DM('POLYGON Z((2 2 4, 10 0 3, 10 5 2, 0 5 1, 2 2 4))', 5);
-- Answer:          POLYGON M((2 2 5, 10 0 5, 10 5 5, 0 5 5, 2 2 5))
```

## See also

* [`ST_Force2D`](../ST_Force2D), [`ST_Force3D`](../ST_Force3D), [`ST_Force4D`](../ST_Force4D), [`ST_UpdateZ`](../ST_UpdateZ)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_Force3DM.java" target="_blank">Source code</a>
