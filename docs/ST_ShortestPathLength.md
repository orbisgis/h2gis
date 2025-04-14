# ST_ShortestPathLength

## Signatures

```sql
-- Input type:
--     TABLE[EDGE_ID, START_NODE, END_NODE[, w][, eo]]
-- Return type:
--     TABLE[SOURCE, DESTINATION, DISTANCE]
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]'[, 'w'],
                      s, d);    -- One-to-One
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]'[, 'w'],
                      s, 'ds'); -- One-to-Several
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]'[, 'w'],
                      s);       -- One-to-All
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]'[, 'w'],
                      'SDT');   -- Many-to-Many
```

## Description

Calculates the length(s) of shortest path(s) among vertices in a
graph. Can be used to calculate distance matrices.

### Input parameters

| Variable      | Meaning                                                                                                                                                                               |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `INPUT_EDGES` | Table containing integer columns `EDGE_ID`, `START_NODE` and `END_NODE`; and optionally a weight column `w` (if the graph is weighted) and/or an edge orientation column `eo` (required if global orientation is not `undirected`) |
| `o`           | Global orientation string: `directed`, `reversed` or `undirected`                                                                                                                     |
| `eo`          | Edge orientation column name indicating individual edge orientations: `1` (directed), `-1` (reversed) or `0` (undirected); required if global orientation is `directed` or `reversed` |
| `w`           | Edge weights column name                                                                                                                                                              |
| `s`           | Source vertex id                                                                                                                                                                      |
| `d`           | Destination vertex id                                                                                                                                                                 |
| `ds`          | Comma-separated destination string: `'dest1, dest2, ...'`                                                                                                                             |
| `SDT`         | Source-Destination table name; must contain columns `SOURCE` and `DESTINATION` containing integer vertex ids                                                                          |

## Examples

```sql
-- Prepare example data. We give an illustration of the graph this
-- represents below. In order to visualize how these distances are
-- calculated, please see the documentation of ST_ShortestPath and
-- ST_ShortestPathTree.
CREATE TABLE EDGES(EDGE_ID INT AUTO_INCREMENT PRIMARY KEY,
                   START_NODE INT,
                   END_NODE INT,
                   WEIGHT DOUBLE,
                   EDGE_ORIENTATION INT);
INSERT INTO EDGES VALUES
    (DEFAULT, 1, 2, 10.0,  1),
    (DEFAULT, 2, 4,  1.0, -1),
    (DEFAULT, 2, 3,  2.0,  1),
    (DEFAULT, 3, 2,  3.0,  1),
    (DEFAULT, 1, 3,  5.0,  1),
    (DEFAULT, 3, 4,  9.0,  1),
    (DEFAULT, 3, 5,  2.0,  1),
    (DEFAULT, 4, 5,  4.0,  1),
    (DEFAULT, 5, 4,  6.0,  1),
    (DEFAULT, 5, 1,  7.0,  0),
    (DEFAULT, 6, 7,  1.0,  1),
    (DEFAULT, 7, 8,  2.0,  1);
```

<img class="displayed" src="../wdo.svg">

### One-to-One

```sql
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 1, 5);
-- | SOURCE | DESTINATION | DISTANCE |
-- |--------|-------------|----------|
-- |      1 |           5 |      7.0 |

-- We can obtain just the distance if we want:
SELECT DISTANCE FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 1, 5);
-- | DISTANCE |
-- |----------|
-- |      7.0 |

-- In an unweighted graph, d(1, 5) is just the number of steps from
-- vertex 1 to vertex 5. They are connected by edge -10.
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        1, 5);
-- | SOURCE | DESTINATION | DISTANCE |
-- |--------|-------------|----------|
-- |      1 |           5 |      1.0 |

-- The distance function is not necessarily symmetric in directed
-- graphs: d(a, b) != d(b, a)
SELECT (SELECT DISTANCE FROM
            ST_ShortestPathLength('EDGES',
                'directed - EDGE_ORIENTATION',
                'WEIGHT', 1, 3)) DIST_1_3,
       (SELECT DISTANCE FROM
            ST_ShortestPathLength('EDGES',
                'directed - EDGE_ORIENTATION',
                'WEIGHT', 3, 1)) DIST_3_1;
-- | DIST_1_3 | DIST_3_1 |
-- |----------|----------|
-- |      5.0 |      9.0 |

-- However, it is symmetric in undirected graphs:
SELECT (SELECT DISTANCE FROM
            ST_ShortestPathLength('EDGES',
                'undirected',
                'WEIGHT', 1, 3)) DIST_1_3,
       (SELECT DISTANCE FROM
            ST_ShortestPathLength('EDGES',
                'undirected',
                'WEIGHT', 3, 1)) DIST_3_1;
-- | DIST_1_3 | DIST_3_1 |
-- |----------|----------|
-- |      5.0 |      5.0 |

-- Vertex 6 is not reachable from vertex 1.
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 1, 6);
-- | SOURCE | DESTINATION | DISTANCE |
-- |--------|-------------|----------|
-- |      1 |           6 | Infinity |
```

### One-to-Several

```sql
-- Here we calculate d(1, 3), d(1, 5) and d(1, 6) in a single
-- request. Since vertex 6 is not reachable from vertex 1, it is not
-- returned in the list.
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 1, '3, 5, 6');
-- | SOURCE | DESTINATION | DISTANCE |
-- |--------|-------------|----------|
-- |      1 |           3 |      5.0 |
-- |      1 |           5 |      7.0 |
```

### One-to-All

```sql
-- Here we calculate d(1, *), i.e., the distance from vertex 1 to
-- all reachable vertices. Notice that vertices 6, 7 and 8 are not
-- reachable from vertex 1, so they do not show up in the list.
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 1);
-- | SOURCE | DESTINATION | DISTANCE |
-- |--------|-------------|----------|
-- |      1 |           4 |     13.0 |
-- |      1 |           3 |      5.0 |
-- |      1 |           2 |      8.0 |
-- |      1 |           5 |      7.0 |
-- |      1 |           1 |      0.0 |

-- The only vertices reachable from vertex 6 are vertices 6, 7 and
-- 8.
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 6);
-- | SOURCE | DESTINATION | DISTANCE |
-- |--------|-------------|----------|
-- |      6 |           7 |      1.0 |
-- |      6 |           8 |      3.0 |
-- |      6 |           6 |      0.0 |
```

### Many-to-Many (distance matrices)

```sql
-- Create a source-destination table:
CREATE TABLE SDT(SOURCE INT,
                 DESTINATION INT) AS
    SELECT A.X, B.X
    FROM SYSTEM_RANGE(1, 8) A,
         SYSTEM_RANGE(1, 8) B;

-- Only vertices reachable from each source are returned.
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 'SDT')
    ORDER BY SOURCE, DESTINATION ASC;
-- | SOURCE | DESTINATION | DISTANCE |
-- |--------|-------------|----------|
-- |      1 |           1 |      0.0 |
-- |      1 |           2 |      8.0 |
-- |      1 |           3 |      5.0 |
-- |      1 |           4 |     13.0 |
-- |      1 |           5 |      7.0 |
-- |      2 |           1 |     11.0 |
-- |      2 |           2 |      0.0 |
-- |      2 |           3 |      2.0 |
-- |      2 |           4 |     10.0 |
-- |      2 |           5 |      4.0 |
-- |      3 |           1 |      9.0 |
-- |      3 |           2 |      3.0 |
-- |      3 |           3 |      0.0 |
-- |      3 |           4 |      8.0 |
-- |      3 |           5 |      2.0 |
-- |      4 |           1 |     11.0 |
-- |      4 |           2 |      1.0 |
-- |      4 |           3 |      3.0 |
-- |      4 |           4 |      0.0 |
-- |      4 |           5 |      4.0 |
-- |      5 |           1 |      7.0 |
-- |      5 |           2 |      7.0 |
-- |      5 |           3 |      9.0 |
-- |      5 |           4 |      6.0 |
-- |      5 |           5 |      0.0 |
-- |      6 |           6 |      0.0 |
-- |      6 |           7 |      1.0 |
-- |      6 |           8 |      3.0 |
-- |      7 |           7 |      0.0 |
-- |      7 |           8 |      2.0 |
-- |      8 |           8 |      0.0 |

-- The following example shows that the distance matrix D' of a graph
-- G' whose edges are obtained from a graph G by reversing the
-- orientation of every edge is the transpose of the distance matric D
-- of G: D' = D_transpose.
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'reversed - EDGE_ORIENTATION',
        'WEIGHT', 'SDT')
    ORDER BY DESTINATION, SOURCE ASC;
-- | SOURCE | DESTINATION | DISTANCE |
-- |--------|-------------|----------|
-- |      1 |           1 |      0.0 |
-- |      2 |           1 |      8.0 |
-- |      3 |           1 |      5.0 |
-- |      4 |           1 |     13.0 |
-- |      5 |           1 |      7.0 |
-- |      1 |           2 |     11.0 |
-- |      2 |           2 |      0.0 |
-- |      3 |           2 |      2.0 |
-- |      4 |           2 |     10.0 |
-- |      5 |           2 |      4.0 |
-- |      1 |           3 |      9.0 |
-- |      2 |           3 |      3.0 |
-- |      3 |           3 |      0.0 |
-- |      4 |           3 |      8.0 |
-- |      5 |           3 |      2.0 |
-- |      1 |           4 |     11.0 |
-- |      2 |           4 |      1.0 |
-- |      3 |           4 |      3.0 |
-- |      4 |           4 |      0.0 |
-- |      5 |           4 |      4.0 |
-- |      1 |           5 |      7.0 |
-- |      2 |           5 |      7.0 |
-- |      3 |           5 |      9.0 |
-- |      4 |           5 |      6.0 |
-- |      5 |           5 |      0.0 |
-- |      6 |           6 |      0.0 |
-- |      7 |           6 |      1.0 |
-- |      8 |           6 |      3.0 |
-- |      7 |           7 |      0.0 |
-- |      8 |           7 |      2.0 |
-- |      8 |           8 |      0.0 |
```

## See also

* [`ST_Accessibility`](../ST_Accessibility),
  [`ST_ShortestPath`](../ST_ShortestPath),
  [`ST_ShortestPathTree`](../ST_ShortestPathTree),
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-network/src/main/java/org/h2gis/network/functions/ST_ShortestPathLength.java" target="_blank">Source code</a>
