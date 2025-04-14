# ST_AsWKT

## Signatures

```sql
VARCHAR ST_AsWKT(GEOMETRY geom);
```

## Description

Converts a Geometry into its Well Known Text value.

```{include} sfs-1-2-1.md
```

## Example

```sql
SELECT ST_AsWKT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))');
-- Answer: POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_AsWKT.java" target="_blank">Source code</a>
