# ST_Length

## Signature

```sql
DOUBLE ST_Length(GEOMETRY linestring);
```

## Description

Returns the length of (multi)`Linestring`.

Length is measured in the units of the spatial reference system.

<div class="note warning">
  <p>Note that (multi)<code>point</code>s, (multi)<code>polygon</code>s or <code>GeometryCollection</code>s return <code>0.0</code>.</p>
</div>

```{include} sfs-1-2-1.md
```

## Examples

```sql
SELECT ST_Length('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: 6.35917360311745

SELECT ST_Length('MULTILINESTRING ((1 1, 1 2, 2 2),
                                   (2 1, 3 1, 3 3))');
-- Answer: 5
```

### Cases where `ST_Length` returns `0`

```sql
SELECT ST_Length('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: 0.0

SELECT ST_Length('POLYGON((1 2, 4 2, 4 6, 1 6, 1 2))');
-- Answer: 0.0

SELECT ST_Length('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)),
                               ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: 0.0

SELECT ST_Length('GEOMETRYCOLLECTION(
                    MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                    LINESTRING(2 1, 1 3, 5 2),
                    POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: 0.0
```

## See also

* [`ST_3DLength`](../ST_3DLength), [`ST_Perimeter`](../ST_Perimeter), [`ST_3DPerimeter`](../ST_3DPerimeter)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_Length.java" target="_blank">Source code</a>
