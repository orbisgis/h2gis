# ST_Simplify

## Signature

```sql
GEOMETRY ST_Simplify(GEOMETRY geom, DOUBLE distance);
```

## Description

Simplifies `geom` using the [Douglas-Peuker][wiki] algorithm with a
distance tolerance of `distance`. Ensures that resulting polygonal
geometries are valid.

<div class="note">
    <h5>The Douglas-Peucker algorithm does not preserve topology.</h5>
    <p>To preserve the topology, use <a
    href="../ST_SimplifyPreserveTopology"><code>ST_SimplifyPreserveTopology</code></a>.
    But note that <code>ST_Simplify</code> is significantly faster.
    </p>
</div>

## Examples

```sql
SELECT ST_Simplify('POLYGON((2 1, 1 2, 2 2, 2 3, 3 3, 3 2,
                             4 2, 4 1, 3 0, 2 0, 2 1))',
                    0.5)
-- Answer: POLYGON((2 1, 1 2, 3 3, 4 1, 3 0, 2 0, 2 1))
```

![](./ST_Simplify.png)

```sql
SELECT ST_Simplify('POLYGON((2 1, 1 2, 2 2, 2 3, 3 3, 3 2,
                             4 2, 4 1, 3 0, 2 0, 2 1))',
                    1)
-- Answer:POLYGON((2 1, 1 2, 3 3, 4 1, 2 1))
```

![](./ST_Simplify_1.png)

```sql
-- If the distance tolerance is too large, the Geometry may be
-- oversimplified:
SELECT ST_Simplify('POLYGON((2 1, 1 2, 2 2, 2 3, 3 3, 3 2,
                             4 2, 4 1, 3 0, 2 0, 2 1))',
                    2)
-- Answer: POLYGON EMPTY

-- POINTs and MULTIPOINTs cannot be further simplified:
SELECT ST_Simplify('MULTIPOINT((190 300), (10 11))', 4);
-- Answer: MULTIPOINT((190 300), (10 11))

-- Simplify a LINESTRING:
SELECT ST_Simplify('LINESTRING(250 250, 280 290, 300 230, 340 300,
                               360 260, 440 310, 470 360, 604 286)',
                   40);
-- Answer: LINESTRING(250 250, 280 290, 300 230, 470 360, 604 286)
```

### Comparison with [`ST_SimplifyPreserveTopology`](../ST_SimplifyPreserveTopology)

{% include simplify-simplifypreserve-cf.html %}

## See also

* [`ST_SimplifyPreserveTopology`](../ST_SimplifyPreserveTopology)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/generalize/ST_Simplify.java" target="_blank">Source code</a>
* JTS [DouglasPeuckerSimplifier#simplify][jts]

[jts]: http://tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/simplify/DouglasPeuckerSimplifier.html#simplify(com.vividsolutions.jts.geom.Geometry, double)
[wiki]: http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
