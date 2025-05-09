# ST_SnapToSelf

## Signature

```sql
GEOMETRY ST_SnapToSelf(GEOMETRY geom, DOUBLE snapTolerance);
GEOMETRY ST_SnapToSelf(GEOMETRY geom, DOUBLE snapTolerance, BOOLEAN clean);
```

## Description

Snap a geometry (`geom`) to itself with a given tolerance (`snapTolerance`).

Optionally, `clean` parameter allow to clean the resulting geometry making sure it is topologically valid. `TRUE` by default.

Snapping a geometry to itself can remove artifacts such as very narrow slivers, gores and spikes.

## Examples

### With `POLYGON`

```sql
SELECT ST_SnapToSelf('POLYGON ((1 1, 1 4, 5.5 4, 6 6, 5 1, 1 1))', 0.5);

-- Answer: POLYGON ((1 1, 1 4, 5.5 4, 5 1, 1 1))
```

![](./ST_SnapToSelf_1.png){align=center}

### With `LINESTRING`

Here `snapTolerance` is bigger than the distance between points [3 1] and [3.5 1]. So the resulting LINESTRING has a new point

```sql
SELECT ST_SnapToSelf('LINESTRING (1 1, 3 1, 5 4, 3.5 1, 6 1)', 1);

-- Answer: LINESTRING (1 1, 3 1, 5 4, 3 1, 3.5 1, 6 1)
```

![](./ST_SnapToSelf_2.png){align=center}

Here `snapTolerance` is smaller than the distance between points [3 1] and [3.5 1]. So nothing changes.

```sql
SELECT ST_SnapToSelf('LINESTRING (1 1, 3 1, 5 4, 3.5 1, 6 1)', 0.2);

-- Answer: LINESTRING (1 1, 3 1, 5 4, 3.5 1, 6 1)
```

![](./ST_SnapToSelf_3.png){align=center}

### With `clean` parameter

#### `clean` is `TRUE`

```sql
SELECT ST_SnapToSelf('POLYGON ((1 1, 3.5 2.5, 1 4, 4 4, 4 2, 6 6, 6 1, 3.5 2, 1 1))', 
                     1, TRUE);

-- Answer: MULTIPOLYGON (((3.5 2, 4 4, 6 6, 6 1, 4 2, 3.5 2)), 
--                       ((3.5 2, 1 4, 4 4, 3.5 2.5, 3.5 2)))
```

![](./ST_SnapToSelf_4.png){align=center}

#### `clean` is `FALSE`

```sql
SELECT ST_SnapToSelf('POLYGON ((1 1, 3.5 2.5, 1 4, 4 4, 4 2, 6 6, 6 1, 3.5 2, 1 1))', 
                     1, FALSE);

-- Answer: POLYGON ((1 1, 3.5 2, 1 4, 4 4, 3.5 2.5, 3.5 2, 4 4, 6 6, 6 1, 4 2, 3.5 2, 1 1))
```

![](./ST_SnapToSelf_5.png){align=center}


## See also

* [`ST_Snap`](../ST_Snap)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/snap/ST_SnapToSelf.java" target="_blank">Source code</a>
