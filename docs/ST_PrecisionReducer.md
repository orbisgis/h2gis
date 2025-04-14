# ST_PrecisionReducer

## Signature

```sql
GEOMETRY ST_PrecisionReducer(GEOMETRY geom, INT n);
```

## Description

Reduces the precision of `geom` to `n` decimal places.

## Examples

```sql
SELECT ST_PrecisionReducer(
            'MULTIPOINT((190.1239999997 300), (10 11.1233))', 3);
-- Answer:   MULTIPOINT((190.124 300), (10 11.123))

```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/generalize/ST_PrecisionReducer.java" target="_blank">Source code</a>
* JTS [GeometryPrecisionReducer#reduce][jts]

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/precision/GeometryPrecisionReducer.html#reduce(com.vividsolutions.jts.geom.Geometry)
