# ST_ClusterWithin

## Signature

```sql
ST_ClusterWithin('tableName', 'geomColumn', 'idColumn', DOUBLE distance) RETURN tableName[THE_GEOM, ID, CLUSTER_ID, CLUSTER_SIZE]
```

## Description

ST_ClusterWithin is a spatial clustering function that groups geometries into clusters based on a maximum distance between them. This function is equivalent to ST_ClusterDBSCAN with minPoints = 0, meaning every geometry is considered as a potential cluster center.



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
                                                   (5, 'Point E', ST_GeomFromText('POINT(20 20)', 4326));

-- Run ST_ClusterWithin with distance=0.5
SELECT * FROM ST_ClusterWithin('sample_points', 'the_geom', 'id', 0.5);

```

### Result

| id | the_geom | cluster_id | cluster_size |
| --- | --- | --- | --- |
| 1 | POINT(0 0) | 1 | 2 |
| 2 | POINT(0.1 0.1) | 1 | 2 |
| 3 | POINT(10 10) | 2 | 1 |
| 4 | POINT(10.1 10.1) | 2 | 1 |
| 5 | POINT(20 20) | NULL | NULL |

## See also

* [`ST_ClusterDBSCAN`](../ST_ClusterDBSCAN), [`ST_ClusterIntersecting`](../ST_ClusterIntersecting)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/clusters/ST_ClusterWithin.java" target="_blank">Source code</a>
