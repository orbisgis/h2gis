# ST_MaxDistance

## Signature

```sql
DOUBLE ST_MaxDistance(GEOMETRY geomA, GEOMETRY geomB);
```

## Description

Returns the 2-dimensional longest distance between the points of two geometries (`geomA` and `geomB`).

If `geomA` and `geomB` are the same, the function will return the longest distance between the two farthest vertices in this geometry.

Distance is measured in the units of the spatial reference system.

### Remark

To return the geometry (`LINESTRING`) that correspond to the longest distance, the user may use [`ST_LongestLine`](../ST_LongestLine).

## Examples

### Cases where `geomA` and `geomB` are different

```sql
SELECT ST_MaxDistance('POLYGON ((0 1, 1 1, 1 0, 0 0, 0 1))', 
                      'POLYGON ((2 3, 2 2, 3 2, 4 3, 2 3))');
-- Answer: 5
```

<img class="displayed" src="../ST_LongestLine_1.png"/>

```sql
SELECT ST_MaxDistance('POLYGON ((0 1, 1 1, 1 0, 0 0, 0 1))', 
		      'LINESTRING (1 3, 4 3)');
-- Answer: 5
```

<img class="displayed" src="../ST_LongestLine_2.png"/>

```sql
SELECT ST_MaxDistance('POLYGON ((0 1, 1 1, 1 0, 0 0, 0 1))', 
		      'POINT (3 2)');
-- Answer: 3,605551275463989 
```

<img class="displayed" src="../ST_LongestLine_3.png"/>

```sql
SELECT ST_MaxDistance('MULTIPOLYGON (((0 1, 1 1, 1 0, 0 0, 0 1)),
				    ((2 3, 2 2, 3 3, 2 3)))', 
		      'POINT (3 2)');
-- Answer: 3,605551275463989
```

<img class="displayed" src="../ST_LongestLine_4.png"/>

### Case where `geomA` is equal to `geomB`

```sql
SELECT ST_MaxDistance('POLYGON ((1 3, 0 0, 3 2, 1 3))', 
		      'POLYGON ((1 3, 0 0, 3 2, 1 3))');
-- Answer: 3,605551275463989
```

<img class="displayed" src="../ST_LongestLine_5.png"/>


## See also

* [`ST_LongestLine`](../ST_LongestLine), [`ST_Length`](../ST_Length), [`ST_ClosestCoordinate`](../ST_ClosestCoordinate)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/distance/ST_MaxDistance.java" target="_blank">Source code</a>
