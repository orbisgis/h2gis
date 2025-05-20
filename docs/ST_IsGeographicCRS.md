# ST_IsGeographicCRS

## Signature

```sql
BOOLEAN ST_IsGeographicCRS(GEOMETRY geom);
```

## Description

Takes a geometry (`geom`) and return `TRUE` is the coordinate system is geographic.

This function will return `FALSE` if:
* the coordinate system is geometric,
* the geometry is null,
* the geometry's SRID is equal to 0.

:::{tip}
**Find the SRID you're looking for**

All available CRS SRIDs may be found by executing the query `SELECT * FROM SPATIAL_REF_SYS;`. Most SRIDs are EPSG, but the `SPATIAL_REF_SYS` table may be enriched by other CRSes.
:::


## Examples

### Case with no SRID

Here we have a POINT whose coordinates are exprimed in WGS84 - [EPSG:4326](https://epsg.io/4326), but no SRID is applied

```sql
SELECT ST_IsGeographicCRS('POINT(-1.53391 47.20259)');

-- Answer: FALSE
```

### Case with a SRID

With the same POINT seen before, we force the SRID to be in WGS84 - [EPSG:4326](https://epsg.io/4326)

```sql
SELECT ST_IsGeographicCRS(ST_SetSRID('POINT(-1.53391 47.20259)', 4326));

-- Answer: TRUE
```

Same POINT, but exprimed and forced in Lambert 93 (legal system in France - [EPSG:2154](https://epsg.io/2154)), which is not a geographic system

```sql
SELECT ST_IsGeographicCRS(ST_SetSRID('POINT(356964.672 6687884.168)', 2154));

-- Answer: FALSE
```

## See also

* [`ST_SetSRID`](../ST_SetSRID), [`ST_SRID`](../ST_SRID), [`UpdateGeometrySRID`], (../UpdateGeometrySRID), [`ST_FindUTMSRID`](../ST_FindUTMSRID), (../UpdateGeometrySRID), [`ST_Transform`](../ST_Transform)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/crs/ST_IsGeographicCRS.java" target="_blank">Source code</a>
