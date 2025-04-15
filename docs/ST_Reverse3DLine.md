# ST_Reverse3DLine

## Signatures

```sql
GEOMETRY ST_Reverse3DLine(GEOMETRY geom);
GEOMETRY ST_Reverse3DLine(GEOMETRY geom, VARCHAR sortOrder);
```

## Description

Potentially reverses `geom` according to the *z*-values of its first
and last coordinates and the optional parameter `sortOrder`, which
can take the following values:

| Value  | Meaning                   |
|--------|---------------------------|
| `asc`  | ascending (default value) |
| `desc` | descending                |

Returns `geom` untouched if the start or end coordinate has no
*z*-value.

:::{note}
**Only the first and last coordinates are considered**.
Intermediate *z*-values have no effect on the sorting.
:::

```{include} other-line-multiline.md
```

## Examples

```sql
-- Reverses the order since the default value of sortOrder is asc
-- and 10 > 0:
SELECT ST_Reverse3DLine(
            'LINESTRING(105 353 10, 150 180, 300 280 0)');
-- Answer:   LINESTRING(300 280 0, 150 180, 105 353 10)

-- Makes no change since the LINESTRING is already in descending
-- order:
SELECT ST_Reverse3DLine(
            'LINESTRING(105 353 10, 150 180, 300 280 0)', 'desc');
-- Answer:   LINESTRING(105 353 10, 150 180, 300 280 0)

-- Puts the LINESTRING in descending order:
SELECT ST_Reverse3DLine(
            'LINESTRING(105 353 0, 150 180, 300 280 10)', 'desc');
-- Answer:   LINESTRING(300 280 10, 150 180, 105 353 0)

-- Puts each component LINESTRING in descending order:
SELECT ST_Reverse3DLine(
            'MULTILINESTRING((1 1 1, 1 6 2, 2 2 1, -1 2 3),
                             (1 2 0, 4 2, 4 6 2))', 'desc');
-- Answer:   MULTILINESTRING((-1 2 3, 2 2 1, 1 6 2, 1 1 1),
--                           (4 6 2, 4 2, 1 2 0))
```

### Non-examples

```sql
-- Returns the Geometry untouched since its first coordinate
-- contains no z-value:
SELECT ST_Reverse3DLine('LINESTRING(1 1, 1 6 2, 2 2, -1 2 3)');
-- Answer:               LINESTRING(1 1, 1 6 2, 2 2, -1 2 3)

-- Returns NULL for Geometries other than LINESTRINGs and
-- MULTILINESTRINGs:
SELECT ST_Reverse3DLine('POLYGON((190 300, 140 180, 300 110,
                                  313 117, 430 270, 190 300))');
-- Answer: NULL
```

## See also

* [`ST_Reverse`](../ST_Reverse)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_Reverse3DLine.java" target="_blank">Source code</a>
