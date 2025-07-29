# ST_SRID

## Signature

```sql
INT ST_SRID(GEOMETRY geom);
```

## Description

Returns SRID value or 0 if input Geometry does not have one.

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_SRID(ST_GeomFromText('POINT(15 25)', 2154));
-- Answer: 2154
```

```sql
SELECT ST_SRID(ST_GeomFromText('LINESTRING(2 1, 1 3, 5 2, 2 1)', 4326));
-- Answer: 4326
```

## See also

* [`ST_SetSRID`](../ST_SetSRID), [`ST_Transform`](../ST_Transform), [`UpdateGeometrySRID`](../UpdateGeometrySRID), [`ST_FindUTMSRID`](../ST_FindUTMSRID), [`ST_GeomFromText`](../ST_GeomFromText), [`ST_IsGeographicCRS`](../ST_IsGeographicCRS)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_SRID.java" target="_blank">Source code</a>