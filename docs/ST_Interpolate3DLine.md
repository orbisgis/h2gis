# ST_Interpolate3DLine

## Signature

```sql
GEOMETRY ST_Interpolate3DLine(GEOMETRY geom);
```

## Description

Interpolate the *z*-values of `geom` based on the *z*-values of its
first and last coordinates.
Does an interpolation on each indiviual Geometry of `geom` if it is
a `GEOMETRYCOLLECTION`.

Returns `geom` untouched if its first or last coordinate has no
*z*-value.

{% include other-line-multiline.html %}

## Examples

```sql
SELECT ST_Interpolate3DLine('LINESTRING(0 0 1, 5 0, 10 0 10)');
-- Answer:                   LINESTRING(0 0 1, 5 0 5.5, 10 0 10)

SELECT ST_Interpolate3DLine(
          'MULTILINESTRING((0 0 0, 5 0, 10 0 10),
                           (0 0 0, 50 0, 100 0 100))');
-- Answer: MULTILINESTRING((0 0 0, 5 0 5, 10 0 10),
--                         (0 0 0, 50 0 50, 100 0 100))
```

### Nonexamples

```sql
-- Returns the Geometry untouched:
SELECT ST_Interpolate3DLine('LINESTRING(0 8, 1 8, 3 8)');
-- Answer: LINESTRING(0 8, 1 8, 3 8)

-- Returns NULL for Geometries other than LINESTRINGs and
-- MULTILINESTRINGs:
SELECT ST_Interpolate3DLine(
            'POLYGON((2 0 1, 2 8 0, 4 8, 4 0, 2 0))');
-- Answer: NULL
```

## See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_Interpolate3DLine.java" target="_blank">Source code</a>
