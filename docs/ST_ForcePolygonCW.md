# ST_ForcePolygonCW

## Signature

```sql
GEOMETRY ST_ForcePolygonCW(GEOMETRY geom);
```

## Description

Forces (`MULTI`)`POLYGON`'s to have:
* a **clockwise** orientation for their exterior ring, 
* and a **counter-clockwise** orientation for their interior rings.

Non-polygonal geometries are returned unchanged.

## Examples

### With `POLYGON`

```sql
SELECT ST_ForcePolygonCW('POLYGON((1 2, 5 2, 5 6, 1 6, 1 2))');

-- Answer: POLYGON ((1 2, 1 6, 5 6, 5 2, 1 2))
```

![](./ST_ForcePolygonCW_1.png){align=center}

### With `MULTIPOLYGON`

```sql
SELECT ST_ForcePolygonCW('MULTIPOLYGON(((1 2, 5 2, 5 6, 1 6, 1 2)), 
                                       ((6 4, 7 4, 7 7, 6 7, 6 4)))');

-- Answer: MULTIPOLYGON (((1 2, 1 6, 5 6, 5 2, 1 2)), 
--                      ((6 4, 6 7, 7 7, 7 4, 6 4)))
```

![](./ST_ForcePolygonCW_2.png){align=center}

### With interior ring

```sql
SELECT ST_ForcePolygonCW('POLYGON((1 2, 5 2, 5 6, 1 6, 1 2),
                                  (2 3, 3 3, 3 5, 2 5, 2 3))');

-- Answer: POLYGON ((1 2, 1 6, 5 6, 5 2, 1 2), 
--                  (2 3, 3 3, 3 5, 2 5, 2 3))
```

![](./ST_ForcePolygonCW_3.png){align=center}

## See also

* [`ST_ForcePolygonCCW`](../ST_ForcePolygonCCW)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_ForcePolygonCW.java" target="_blank">Source code</a>
