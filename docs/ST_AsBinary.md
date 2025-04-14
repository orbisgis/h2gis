# ST_AsBinary

## Signature

```sql
BINARY ST_AsBinary(GEOMETRY geom);
```

## Description

Converts `geom` into its Well Known Binary value.

```{include} sfs-1-2-1.md
```

## Example

```sql
SELECT ST_AsBinary(
    ST_GeomFromText('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))', 4326));
-- Answer: 0020000003000010e600000001000000050000000000000000
--    000000000000000000000000000000003ff00000000000003ff0000
--    0000000003ff00000000000003ff000000000000000000000000000
--    0000000000000000000000000000000000
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_AsBinary.java" target="_blank">Source code</a>
