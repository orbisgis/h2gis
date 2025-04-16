# ST_Collect

## Signatures

```sql
MULTIPOINT         ST_Collect(POINT geom);
MULTILINESTRING    ST_Collect(LINESTRING geom);
MULTIPOLYGON       ST_Collect(POLYGON geom);
GEOMETRYCOLLECTION ST_Collect(GEOMETRY geom);
```

## Description

This aggregate function constructs a `GEOMETRYCOLLECTION` from a column of mixed dimension Geometries.

If there is only `POINT`s in the column of Geometries, a `MULTIPOINT` is returned. Same process with `LINESTRING`s and `POLYGON`s.

:::{Warning}
**This function is an alias for `ST_Accum`. For more details, please consult [this page](./ST_Accum).**
:::


## See also

* [`ST_Accum`](../ST_Accum), [`ST_Union`](../ST_Union), [`ST_Explode`](../ST_Explode)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/aggregate/ST_Collect.java" target="_blank">Source code</a>
