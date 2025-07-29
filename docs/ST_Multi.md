# ST_Multi

## Signature

```sql
GEOMETRY ST_Multi(GEOMETRY geom);
```

## Description

Takes a geometry (`geom`) and converts it as a geometry collection (MULTI).

If the geometry is already a collection (MULTI), it is returned unchanged.

## Examples

### With `POINT`, `LINESTRING` and `POLYGON`

```sql
SELECT ST_MULTI('POINT(0 0)');

-- Answer: MULTIPOINT ((0 0))
```

```sql
SELECT ST_MULTI('LINESTRING (130 180, 260 170)');

-- Answer: MULTILINESTRING ((130 180, 260 170))
```

```sql
SELECT ST_MULTI('POLYGON((0 0,0 1,1 1,1 0,0 0))');

-- Answer: MULTIPOLYGON (((0 0, 0 1, 1 1, 1 0, 0 0)))
```

### A `MULTI` geometry remains the same

```sql
SELECT ST_MULTI('MULTIPOLYGON (((114 186, 180 186, 180 120, 114 120, 114 186)), 
	                          ((220 170, 250 170, 250 120, 220 120, 220 170)))');

-- Answer:       MULTIPOLYGON (((114 186, 180 186, 180 120, 114 120, 114 186)), 
--                            ((220 170, 250 170, 250 120, 220 120, 220 170)))
```

### A `GEOMETRYCOLLECTION` geometry remains the same

```sql
SELECT ST_MULTI('GEOMETRYCOLLECTION (LINESTRING (30 80, 225 86), 
	                                 POINT (136 124))');

-- Answer: GEOMETRYCOLLECTION (LINESTRING (30 80, 225 86), POINT (136 124))
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_Multi.java" target="_blank">Source code</a>
