---
layout: docs
title: ST_ShortestPathLength
category: applications/h2network
is_function: true
description: Calculate length(s) of shortest path(s) between vertices in a graph
prev_section: ST_ShortestPath
next_section:
permalink: /docs/dev/ST_ShortestPathLength/
---

### Signatures

{% highlight mysql %}
-- Return type:
--     TABLE[SOURCE, DESTINATION, DISTANCE]
-- One-to-All
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]', s);
-- Many-to-Many
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]', 'sdt');
-- One-to-One
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]', s, d);
-- One-to-Several
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]', s, 'ds');
-- One-to-All Weighted
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]', 'w', s);
-- Many-to-Many Weighted
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]', 'w', 'sdt');
-- One-to-One Weighted
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]', 'w', s, d);
-- One-to-Several Weighted
ST_ShortestPathLength('INPUT_EDGES', 'o[ - eo]', 'w', s, 'ds');
{% endhighlight %}

### Description

Calculates the length(s) of shortest path(s) among vertices in a
graph.

##### Input parameters

| Variable      | Meaning                                                                                                                                                                               |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `INPUT_EDGES` | Table containing integer columns `EDGE_ID`, `START_NODE` and `END_NODE`, and optionally an edge orientation column `eo` (required if global orientation is not `undirected`)          |
| `o`           | Global orientation string: `directed`, `reversed` or `undirected`                                                                                                                     |
| `eo`          | Edge orientation column name indicating individual edge orientations: `1` (directed), `-1` (reversed) or `0` (undirected); required if global orientation is `directed` or `reversed` |
| `w`           | Edge weights column name                                                                                                                                                              |
| `s`           | Source vertex id                                                                                                                                                                      |
| `d`           | Destination vertex id                                                                                                                                                                 |
| `sdt`         | Source-Destination table name; must contain columns `SOURCE` and `DESTINATION` containing integer vertex ids                                                                          |
| `ds`          | Comma-separated destination string: `'dest1, dest2, ...'`                                                                                                                             |

### Examples

{% highlight mysql %}
CREATE TABLE EDGES(EDGE_ID INT AUTO_INCREMENT PRIMARY KEY,
                   START_NODE INT,
                   END_NODE INT,
                   WEIGHT DOUBLE,
                   EDGE_ORIENTATION INT);
INSERT INTO EDGES VALUES
    (DEFAULT, 1, 2, 10.0, 1),
    (DEFAULT, 2, 4, 1.0, -1),
    (DEFAULT, 2, 3, 2.0,  1),
    (DEFAULT, 3, 2, 3.0,  1),
    (DEFAULT, 1, 3, 5.0,  1),
    (DEFAULT, 3, 4, 9.0,  1),
    (DEFAULT, 3, 5, 2.0,  1),
    (DEFAULT, 4, 5, 4.0,  1),
    (DEFAULT, 5, 4, 6.0,  1),
    (DEFAULT, 5, 1, 7.0,  0),
    (DEFAULT, 6, 7, 1.0, 1),
    (DEFAULT, 7, 8, 2.0, 1);

SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 1, 5);
-- SOURCE  	DESTINATION  	DISTANCE
-- 1	5	7.0

-- We can obtain just the distance if we want:
SELECT DISTANCE FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 1, 5);
-- DISTANCE
-- 7.0

-- Vertex 6 is not reachable from vertex 3.
SELECT * FROM
    ST_ShortestPathLength('EDGES',
        'directed - EDGE_ORIENTATION',
        'WEIGHT', 3, 6);
-- SOURCE  	DESTINATION  	DISTANCE
-- 3	6	Infinity
{% endhighlight %}

##### See also

* [`ST_ShortestPath`](../ST_ShortestPath),
  [`ST_Accessibility`](../ST_Accessibility)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_ShortestPathLength.java" target="_blank">Source code</a>
