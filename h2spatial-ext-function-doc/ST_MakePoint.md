### Name
`ST_MakePoint` -- construct a `POINT` from two or three coordinates.

### Signatures

```mysql
POINT ST_MakePoint(double x, double y);
POINT ST_MakePoint(double x, double y, double z);
```

### Description

Constructs a `POINT` from `x` and `y` (and possibly `z`).

### Examples

```mysql
SELECT ST_MakePoint(1.4, -3.7);
```
Answer:     `POINT(1.4 -3.7)`
```mysql
SELECT ST_MakePoint(1.4, -3.7, 6.2);
```
Answer:     `POINT(1.4 -3.7 6.2)`
