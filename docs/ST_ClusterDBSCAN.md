# ST_ClusterDBSCAN

## Signature

```sql
tableName[THE_GEOM, ID] ST_ClusterDBSCAN('tableName', 'geomColumn', 'idColumn', DOUBLE eps, DOUBLE minPoints)
```

## Description
`ST_ClusterDBSCAN` is a spatial clustering function that groups geometries into clusters using the 
DBSCAN (Density-Based Spatial Clustering of Applications with Noise) algorithm. 
This function is useful for identifying groups of points that are close to each other in space.
 - eps  = The maximum distance between two points to be considered in the same neighborhood (must be greater than 0)
 - minPoints = The minimum number of points required to form a cluster.

## Example

```sql
-- Create a table to store points
CREATE TABLE sample_points (
                               id INT PRIMARY KEY,
                               name VARCHAR(50),
                               the_geom GEOMETRY(POINT, 4326)  -- Using SRID 4326 (WGS84)
);

-- Insert sample points
INSERT INTO sample_points (id, name, the_geom) VALUES
                                                   (1, 'Point A', ST_GeomFromText('POINT(0 0)', 4326)),
                                                   (2, 'Point B', ST_GeomFromText('POINT(0.1 0.1)', 4326)),
                                                   (3, 'Point C', ST_GeomFromText('POINT(10 10)', 4326)),
                                                   (4, 'Point D', ST_GeomFromText('POINT(10.1 10.1)', 4326)),
                                                   (5, 'Point E', ST_GeomFromText('POINT(10.2 10.2)', 4326)),
                                                   (6, 'Point F', ST_GeomFromText('POINT(20 20)', 4326));
-- Run ST_ClusterDBSCAN with eps=0.5 and minPoints=2

SELECT *  ST_ClusterDBSCAN('sample_points', 'the_geom', 'id', 0.5, 2);

```

### Result

| id | the_geom | cluster_id | cluster_size |
| --- | --- | --- | --- |
| 1 | POINT(0 0) | 1 | 2 |
| 2 | POINT(0.1 0.1) | 1 | 2 |
| 3 | POINT(10 10) | 2 | 3 |
| 4 | POINT(10.1 10.1) | 2 | 3 |
| 5 | POINT(10.2 10.2) | 2 | 3 |
| 6 | POINT(20 20) | NULL | NULL |


## See also

* [`ST_ClusterIntersecting`](../ST_ClusterIntersecting), [`ST_ClusterWithin`](../ST_ClusterWithin)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/clusters/ST_ClusterDBScan.java" target="_blank">Source code</a>
