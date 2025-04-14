# ST_ProjectPoint

## Signature

```sql
GEOMETRY ST_ProjectPoint(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

Project a `POINT` (`geomA`) along a `LINESTRING` (`geomB`). If the `POINT` projected is out of the `LINESTRING` the first (or last) `POINT` on the `LINESTRING` will be returned, otherwise the input `POINT`.


## Examples

```sql
SELECT ST_PROJECTPOINT('POINT(1 2)',
                       'LINESTRING(0 0, 4 2)') as THE_GEOM;
-- Answer: POINT (1.6 0.8)
```
![](./ST_ProjectPoint_1.png){align=center}

```sql
SELECT ST_PROJECTPOINT('POINT(1 2)',
                       'MULTILINESTRING ((0 0, 4 2), (1 4, 2 2))') as THE_GEOM;
-- Answer: POINT (1.8 2.4)
```
![](./ST_ProjectPoint_2.png){align=center}

```sql
SELECT ST_PROJECTPOINT('POINT(4 3)',
                       'LINESTRING (0 0, 4 2)') as THE_GEOM;
-- Answer: POINT (4 2)
```
![](./ST_ProjectPoint_3.png){align=center}

```sql
SELECT ST_PROJECTPOINT('POINT(4 3)',
                       'POLYGON ((2 4, 0 2, 3 0, 2 4))') as THE_GEOM;
-- Answer: NULL
```
![](./ST_ProjectPoint_4.png){align=center}


## See also

* [`ST_ShortestLine`](../ST_ShortestLine), [`ST_LocateAlong`](../ST_LocateAlong), [`ST_ClosestPoint`](../ST_ClosestPoint)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/distance/ST_ProjectPoint.java" target="_blank">Source code</a>
