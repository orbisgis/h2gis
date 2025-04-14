# ST_IsValid

## Signature

```sql
BOOLEAN ST_IsValid(GEOMETRY geom);
```

## Description

Returns true if `geom` is valid.

{% include equivalence.html equiv='ST_IsValid(geom) = ARRAY_GET(ST_IsValidDetail(geom), 1)' %}

## Examples

```sql
SELECT ST_IsValid('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))');
-- Answer:    TRUE

SELECT ST_IsValid('POLYGON((0 0, 10 0, 10 5, 6 -2, 0 0))');
-- Answer:    FALSE
```

<img class="displayed" src="../ST_IsValid.png"/>

## See also

* [`ST_IsValidDetail`](../ST_IsValidDetail),
  [`ST_IsValidReason`](../ST_IsValidReason)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_IsValid.java" target="_blank">Source code</a>
* JTS [IsValidOp][jts]

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/operation/valid/IsValidOp.html
