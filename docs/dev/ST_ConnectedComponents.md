---
layout: docs
title: ST_ConnectedComponents
category: applications/h2network
is_function: true
description: Calculate the (strongly) connected components of a graph
prev_section: ST_Accessibility
next_section: ST_Graph
permalink: /docs/dev/ST_ConnectedComponents/
---

### Signatures

{% highlight mysql %}
-- Creates two tables:
--     INPUT_EDGES_NODE_CC[NODE_ID, CONNECTED_COMPONENT]
--     INPUT_EDGES_EDGE_CC[EDGE_ID, CONNECTED_COMPONENT]
ST_ConnectedComponents('INPUT_EDGES', 'o[ - eo]');
{% endhighlight %}

### Description

Calculates the [connected components][cc] (for undirected graphs) or
[strongly connected components][scc] (for directed graphs) of a
graph.
Produces two tables (nodes and edges) containing a node or edge ID
and a connected component ID.

##### Input parameters

| Variable      | Meaning                                                                                                                                                                               |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `INPUT_EDGES` | Edges table produced by `ST_Graph` from table `input`                                                                                                                                 |
| `o`           | Global orientation string: `directed`, `reversed` or `undirected`                                                                                                                     |
| `eo`          | Edge orientation column name indicating individual edge orientations: `1` (directed), `-1` (reversed) or `0` (undirected); required if global orientation is `directed` or `reversed` |

<div class="note">
  <h5>Edges in no strongly connected component are assigned a connected
  component ID of -1.</h5>
  <p>Such edges have a start nodes and end nodes in different strongly
  connected components.</p>
</div>

<div class="note warning">
  <h5>Connected component IDs are not guaranteed to be the same
  after each execution of <code>ST_ConnectedComponents</code>.</h5>
  <p>But of course, the vertex and edge set partitions they
  represent remain consistent. In the examples below, you may have
  to adjust certain requests according to how
  <code>ST_ConnectedComponents</code> numbers the connected
  components.</p>
</div>

### Examples

{% highlight mysql %}
-- Prepare example data:
DROP TABLE IF EXISTS EDGES;
CREATE TABLE EDGES(EDGE_ID INT AUTO_INCREMENT PRIMARY KEY,
                   START_NODE INT,
                   END_NODE INT,
                   EDGE_ORIENTATION INT);
INSERT INTO EDGES(START_NODE, END_NODE, EDGE_ORIENTATION)
    VALUES (1, 2, 1),
           (2, 3, 1),
           (2, 5, 1),
           (2, 6, 1),
           (3, 4, 1),
           (3, 7, 1),
           (4, 3, 1),
           (4, 8, 1),
           (5, 1, 1),
           (5, 6, 1),
           (6, 7, 1),
           (7, 6, 1),
           (8, 4, 1),
           (8, 7, 1),
           (9, 10, 1),
           (10, 9, 1),
           (10, 11, 1),
           (12, 12, 1);

SELECT * FROM EDGES;
-- | EDGE_ID | START_NODE | END_NODE | EDGE_ORIENTATION |
-- |---------|------------|----------|------------------|
-- |       1 |          1 |        2 |                2 |
-- |       2 |          2 |        3 |                1 |
-- |       3 |          2 |        5 |                1 |
-- |       4 |          2 |        6 |                1 |
-- |       5 |          3 |        4 |                1 |
-- |       6 |          3 |        7 |                1 |
-- |       7 |          4 |        3 |                1 |
-- |       8 |          4 |        8 |                1 |
-- |       9 |          5 |        1 |                1 |
-- |      10 |          5 |        6 |                1 |
-- |      11 |          6 |        7 |                1 |
-- |      12 |          7 |        6 |                1 |
-- |      13 |          8 |        4 |                1 |
-- |      14 |          8 |        7 |                1 |
-- |      15 |          9 |       10 |                1 |
-- |      16 |         10 |        9 |                1 |
-- |      17 |         10 |       11 |                1 |
-- |      18 |         12 |       12 |                1 |

-- Do the SCC calculation and diplay the results:
CALL ST_ConnectedComponents('EDGES', 'directed - EDGE_ORIENTATION');

SELECT * FROM EDGES_NODE_CC
    ORDER BY CONNECTED_COMPONENT ASC;
-- | NODE_ID | CONNECTED_COMPONENT |
-- |---------|---------------------|
-- |      12 |                   1 |
-- |       9 |                   2 |
-- |      10 |                   2 |
-- |      11 |                   3 |
-- |       5 |                   4 |
-- |       2 |                   4 |
-- |       1 |                   4 |
-- |       8 |                   5 |
-- |       3 |                   5 |
-- |       4 |                   5 |
-- |       6 |                   6 |
-- |       7 |                   6 |

SELECT * FROM EDGES_EDGE_CC
    ORDER BY CONNECTED_COMPONENT ASC;
-- | EDGE_ID | CONNECTED_COMPONENT |
-- |---------|---------------------|
-- |      14 |                  -1 |
-- |       6 |                  -1 |
-- |      17 |                  -1 |
-- |      10 |                  -1 |
-- |       4 |                  -1 |
-- |       2 |                  -1 |
-- |      18 |                   1 |
-- |      16 |                   2 |
-- |      15 |                   2 |
-- |       1 |                   4 |
-- |       3 |                   4 |
-- |       9 |                   4 |
-- |      13 |                   5 |
-- |       8 |                   5 |
-- |       7 |                   5 |
-- |       5 |                   5 |
-- |      11 |                   6 |
-- |      12 |                   6 |

-- Count the number of edges in each SCC:
DROP TABLE IF EXISTS EDGE_CC_TOTALS;
CREATE TABLE EDGE_CC_TOTALS AS
    SELECT CONNECTED_COMPONENT CC,
           COUNT(CONNECTED_COMPONENT) CC_COUNT
    FROM EDGES_EDGE_CC
    GROUP BY CC
    ORDER BY CC_COUNT DESC;

SELECT * FROM EDGE_CC_TOTALS;
-- | CC | CC_COUNT |
-- |----|----------|
-- | -1 |        6 |
-- |  5 |        4 |
-- |  4 |        3 |
-- |  2 |        2 |
-- |  6 |        2 |
-- |  1 |        1 |

-- Creating these indices will greatly speed up the following
-- calculations.
CREATE INDEX ON EDGES(EDGE_ID);
CREATE INDEX ON EDGES_EDGE_CC(EDGE_ID);

-- Select the largest SCC:
DROP TABLE IF EXISTS EDGES_LARGEST;
CREATE TABLE EDGES_LARGEST AS
    SELECT A.*, B.CONNECTED_COMPONENT CC
    FROM EDGES A, EDGES_EDGE_CC B
    WHERE A.EDGE_ID=B.EDGE_ID
    AND B.CONNECTED_COMPONENT=5;

SELECT * FROM EDGES_LARGEST;
-- | EDGE_ID | START_NODE | END_NODE | EDGE_ORIENTATION | CC |
-- |---------|------------|----------|------------------|----|
-- |       5 |          3 |        4 |                1 |  5 |
-- |       7 |          4 |        3 |                1 |  5 |
-- |       8 |          4 |        8 |                1 |  5 |
-- |      13 |          8 |        4 |                1 |  5 |

-- We can also select the edges which are in no SCC:
DROP TABLE IF EXISTS EDGES_NO_SCC;
CREATE TABLE EDGES_NO_SCC AS
    SELECT A.*, B.CONNECTED_COMPONENT CC
    FROM EDGES A, EDGES_EDGE_CC B
    WHERE A.EDGE_ID=B.EDGE_ID
    AND B.CONNECTED_COMPONENT=-1;

SELECT * FROM EDGES_NO_SCC;
-- | EDGE_ID | START_NODE | END_NODE | EDGE_ORIENTATION | CC |
-- |---------|------------|----------|------------------|----|
-- |       2 |          2 |        3 |                1 | -1 |
-- |       4 |          2 |        6 |                1 | -1 |
-- |       6 |          3 |        7 |                1 | -1 |
-- |      10 |          5 |        6 |                1 | -1 |
-- |      14 |          8 |        7 |                1 | -1 |
-- |      17 |         10 |       11 |                1 | -1 |
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_ConnectedComponents.java" target="_blank">Source code</a>

[cc]: http://en.wikipedia.org/wiki/Connected_component_(graph_theory)
[scc]: http://en.wikipedia.org/wiki/Strongly_connected_component
