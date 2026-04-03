# ST_ClusterIntersecting

## Signature

```sql
tableName[THE_GEOM, ID] ST_ClusterIntersecting('tableName', 'geomColumn', 'idColumn')
```

## Description
`ST_ClusterIntersecting` is a spatial clustering function that groups geometries into clusters based on geometric intersections.

This function is useful for identifying groups of geometries that intersect with each other.

Each geometry that intersects with at least one other geometry will be part of a cluster.
Geometries that do not intersect with any other geometry will be marked as noise (NULL cluster_id).


## Example

```sql
-- Create a table to store polygons
CREATE TABLE sample_polygons (
                                 id INT PRIMARY KEY,
                                 name VARCHAR(50),
                                 the_geom GEOMETRY(POLYGON, 4326)  -- Using SRID 4326 (WGS84)
);

-- Insert sample polygons
INSERT INTO sample_polygons (id, name, the_geom) VALUES
                                                     (1, 'Polygon A', ST_GeomFromText('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))', 4326)),
                                                     (2, 'Polygon B', ST_GeomFromText('POLYGON((0.5 0.5, 1.5 0.5, 1.5 1.5, 0.5 1.5, 0.5 0.5))', 4326)),
                                                     (3, 'Polygon C', ST_GeomFromText('POLYGON((10 10, 11 10, 11 11, 10 11, 10 10))', 4326)),
                                                     (4, 'Polygon D', ST_GeomFromText('POLYGON((10.5 10.5, 11.5 10.5, 11.5 11.5, 10.5 11.5, 10.5 10.5))', 4326)),
                                                     (5, 'Polygon E', ST_GeomFromText('POLYGON((20 20, 21 20, 21 21, 20 21, 20 20))', 4326));

-- Run ST_ClusterIntersecting
SELECT * FROM ST_ClusterIntersecting('sample_polygons', 'the_geom', 'id');

```

### Result

| id | the_geom | cluster_id | cluster_size |
| --- | --- | --- | --- |
| 1 | POLYGON((0 0, 1 0, 1 1, 0 1, 0 0)) | 1 | 2 |
| 2 | POLYGON((0.5 0.5, 1.5 0.5, 1.5 1.5, 0.5 1.5, 0.5 0.5)) | 1 | 2 |
| 3 | POLYGON((10 10, 11 10, 11 11, 10 11, 10 10)) | 2 | 2 |
| 4 | POLYGON((10.5 10.5, 11.5 10.5, 11.5 11.5, 10.5 11.5, 10.5 10.5)) | 2 | 2 |
| 5 | POLYGON((20 20, 21 20, 21 21, 20 21, 20 20)) | NULL | NULL |

## See also

* [`ST_ClusterDBSCAN`](../ST_ClusterDBSCAN), [`ST_ClusterWithin`](../ST_ClusterWithin)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/clusters/ST_ClusterIntersecting.java" target="_blank">Source code</a>
