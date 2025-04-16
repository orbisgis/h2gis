# ST_Transform

## Signature

```sql
GEOMETRY ST_Transform(GEOMETRY geom, INT srid);
```

## Description

Transforms `geom` from its original coordinate reference system (CRS) to the
CRS specified by `srid`.

:::{tip}
**Find the SRID you're looking for**

All available CRS SRIDs may be found by executing the query `SELECT * FROM SPATIAL_REF_SYS;`. Most SRIDs are EPSG, but the `SPATIAL_REF_SYS` table may be enriched by other CRSes.
:::

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_Transform(ST_GeomFromText('POINT(584173 2594514)', 27572), 4326);
-- Answer: POINT(2.1145411092971056 50.345602339855326)
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/crs/ST_Transform.java" target="_blank">Source code</a>
