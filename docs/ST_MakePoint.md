# ST_MakePoint

## Signatures

```sql
POINT ST_MakePoint(DOUBLE x, DOUBLE y);
POINT ST_MakePoint(DOUBLE x, DOUBLE y, DOUBLE z);
```

## Description

Constructs a `POINT` from `x` and `y` (and possibly `z`).

## Examples

```sql
SELECT ST_MakePoint(1.4, -3.7);
-- Answer:     POINT(1.4 -3.7)
```

![](./ST_MakePoint_1.png)

```sql
SELECT ST_MakePoint(1.4, -3.7, 6.2);
-- Answer:     POINT(1.4 -3.7 6.2)
```

![](./ST_MakePoint_2.png)

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/create/ST_MakePoint.java" target="_blank">Source code</a>
