# ST_MakeEnvelope

## Signatures

```sql
POLYGON ST_MakeEnvelope(DOUBLE xmin, DOUBLE ymin,
                        DOUBLE xmax, DOUBLE ymax);
POLYGON ST_MakeEnvelope(DOUBLE xmin, DOUBLE ymin,
                        DOUBLE xmax, DOUBLE ymax, INT srid);
```

## Description

Creates a rectangular `POLYGON` formed `xmin`, `xmax`, `ymin` and
`ymax`. The user may specify an `srid`.

## Examples

```sql
SELECT ST_MakeEnvelope(0, 0, 1, 1);
-- Answer: POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))
```

```sql
SELECT ST_MakeEnvelope(0, 0, 1, 1, 4326);
-- Answer: POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))
```

```sql
SELECT ST_MakeEnvelope(0, -1, 2, 3);
-- Answer: POLYGON((0 -1, 2 -1, 2 3, 0 3, 0 -1))
```

```sql
SELECT ST_SRID(ST_MakeEnvelope(0, 0, 1, 1, 4326));
-- Answer: 4326
```

## See also

* [`ST_Envelope`](../ST_Envelope)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/create/ST_MakeEnvelope.java" target="_blank">Source code</a>
