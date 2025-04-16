# ST_SetSRID

## Signatures

```sql
GEOMETRY ST_SetSRID(GEOMETRY geom, INT srid);
```

## Description

Returns a copy of `geom` with spatial reference id set to `srid`.

:::{Warning}
**`ST_SetSRID` does not actually change the SRID of `geom`.
  For this purpose, use [`ST_Transform`](./ST_Transform-indices) function.**
:::

```{include} sfs-1-2-1.md
```

## Examples

```sql
CREATE TABLE test_srid(the_geom GEOMETRY);
INSERT INTO test_srid VALUES (
    ST_GeomFromText('POINT(15 25)', 27572));
SELECT ST_SRID(ST_SETSRID(the_geom, 5321)) trans,
       ST_SRID(the_geom) original FROM test_srid;
-- Answer:
--    | TRANS | ORIGINAL |
--    |-------|----------|
--    |  5321 |  27572   |
```

## See also

* [`ST_Transform`](../ST_Transform), [`ST_SRID`](../ST_SRID)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/crs/ST_SetSRID.java" target="_blank">Source code</a>
