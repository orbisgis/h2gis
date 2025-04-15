# ST_GeomFromText

## Signatures

```sql
GEOMETRY ST_GeomFromText(VARCHAR wkt);
GEOMETRY ST_GeomFromText(VARCHAR wkt, INT srid);
```

## Description

Converts the Well Known Text `wkt` into a Geometry with spatial reference id `srid`. 
The default value of `srid` is 0.

```{include} z-coord-warning.md
```

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_GeomFromText('POINT(2 3)', 27572);
```
Answer: POINT(2 3)

```sql
SELECT ST_SRID(ST_GeomFromText('LINESTRING(1 3, 1 1, 2 1)'));
```
Answer: 0

```sql
SELECT ST_GeomFromText('POLYGON((0 0 -1, 2 0 2, 2 1 3, 0 0 -1))');
```
Answer: POLYGON((0 0, 2 0, 2 1, 0 0))


## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_GeomFromText.java" target="_blank">Source code</a>
