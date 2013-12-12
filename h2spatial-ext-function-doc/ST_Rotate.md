**Description**: `ST_MakePoint` rotates a geometry by a given angle (in
radians) about the geometry's center or about the given point.

### Example usage

```mysql
SELECT ST_Rotate('LINESTRING(1 3, 1 1, 2 1)'::Geometry, pi()),
```
Answer:    `LINESTRING(2 1, 2 3, 1 3)`
```mysql
ST_Rotate('LINESTRING(1 3, 1 1, 2 1)'::Geometry, pi() / 3), 
```
Answer:
```
LINESTRING(0.3839745962155607 2.0669872981077813,
           2.1160254037844384 1.0669872981077806,
           2.6160254037844384 1.933012701892219)
```
```mysql
ST_Rotate('LINESTRING(1 3, 1 1, 2 1)'::Geometry, -pi()/2, ST_GeomFromText('POINT(2 1)')) 
```
Answer:    `LINESTRING(4 1, 2 2, 2 1)`
```mysql
ST_Rotate('LINESTRING(1 3, 1 1, 2 1)'::Geometry, pi()/2, 1.0, 1.0), 
```
Answer:    `LINESTRING(-1 1, 1 1, 1 2)`
