# ST_3DLength

## Signature

```sql
DOUBLE ST_3DLength(GEOMETRY Linestring);
```

## Description

Returns the 3D length of a (multi)`Linestring`.

In the case of a 2D Geometry, `ST_3DLength` returns the same value as
`ST_Length`.

<div class="note warning">
  <p>Note that (multi)<code>point</code>s, (multi)<code>polygon</code>s or <code>GeometryCollection</code>s return <code>0.0</code>.</p>
</div>

## Examples

```sql
SELECT ST_3DLength('LINESTRING(1 4, 15 7, 16 17)');
-- Answer: 24.367696684397245 = SQRT(205) + SQRT(101)

SELECT ST_3DLength('LINESTRING(1 4 3, 15 7 9, 16 17 22)');
-- Answer: 31.955851421415005 = SQRT(241) + SQRT(270)

SELECT ST_3DLength('MULTILINESTRING((1 4 3, 15 7 9, 16 17 22),
                                    (0 0 0, 1 0 0, 1 2 0, 0 2 1))');
-- Answer: 36.3700649837881 = SQRT(241) + SQRT(270) + 3 + SQRT(2)
```

## See also

* [`ST_Length`](../ST_Length), [`ST_Perimeter`](../ST_Perimeter), [`ST_3DPerimeter`](../ST_3DPerimeter)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_3DLength.java" target="_blank">Source code</a>
