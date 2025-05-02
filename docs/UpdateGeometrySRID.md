# UpdateGeometrySRID

### Signatures

```sql
BOOLEAN UpdateGeometrySRID(VARCHAR tableName, 
                           GEOMETRY geom, 
                           INT srid);
```

### Description

Updates the `srid` of all features in a geometry column (`geom`) from a table (`tableName`).

Returns `TRUE` if the `srid` has been updated.

:::{Warning}
  `UpdateGeometrySRID` does not to actually change the projection of `geom`.
  For this purpose, use [`ST_Transform`](../ST_Transform)
:::


### Examples

Create a table and insert a POINT with a SRID equal to 0
```sql
CREATE TABLE GEO_POINT (THE_GEOM GEOMETRY(POINT));
INSERT INTO GEO_POINT VALUES('SRID=0;POINT(0 0)');
```

Check the SRID

```sql
SELECT ST_SRID(THE_GEOM) as SRID FROM GEO_POINT;
```
Answer: SRID = `0`

Update the SRID

```sql
SELECT UpdateGeometrySRID('GEO_POINT','THE_GEOM',4326);
```

And check the SRID
```sql
SELECT ST_SRID(THE_GEOM) as SRID FROM GEO_POINT;
```
Answer: SRID = `4326`


##### See also

* [`ST_Transform`](../ST_Transform), [`ST_SRID`](../ST_SRID), [`ST_SetSRID`](../ST_SetSRID)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/crs/UpdateGeometrySRID.java" target="_blank">Source code</a>