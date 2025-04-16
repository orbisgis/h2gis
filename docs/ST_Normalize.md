# ST_Normalize

## Signature

```sql
GEOMETRY ST_Normalize(GEOMETRY geom);
```

## Description

Converts a Geometry to its normal (or canonical) form.
The definitions for normal form use the standard lexicographical
ordering on coordinates.

## Examples

```sql
SELECT ST_Normalize('POLYGON((2 4, 1 3, 2 1, 6 1, 6 3, 4 4, 2 4))');
-- Answer:           POLYGON((1 3, 2 4, 4 4, 6 3, 6 1, 2 1, 1 3))
```

```sql
SELECT ST_Normalize('MULTIPOINT((2 2), (2 5), (10 3), (7 1),
                                (5 1), (5 3))');
-- Answer:           MULTIPOINT((2 2), (2 5), (5 1), (5 3),
--                              (7 1), (10 3))
```

```sql
SELECT ST_Normalize('LINESTRING(3 1, 6 1, 6 3, 3 3, 1 1)');
-- Answer:           LINESTRING(1 1, 3 3, 6 3, 6 1, 3 1)
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_Normalize.java" target="_blank">Source code</a>
* JTS [Geometry#normalize][jts]

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/geom/Geometry.html#normalize()
