**Description**: `ST_MakePoint` constructs a `POINT` from two or three coordinates.

### Example usage

```mysql
SELECT ST_MakePoint(1.4, -3.7);
```
Answer:     `POINT(1.4 -3.7)`
```mysql
SELECT ST_MakePoint(1.4, -3.7, 6.2);
```
Answer:     `POINT(1.4 -3.7 6.2)`
