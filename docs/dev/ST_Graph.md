---
layout: docs
title: ST_Graph
category: applications/h2network
is_function: true
description: Produce nodes and edges tables from an input table containing <code>(MULTI)LINESTRINGS</code>
prev_section: ST_ConnectedComponents
next_section: ST_GraphAnalysis
permalink: /docs/dev/ST_Graph/
---

### Signatures

{% highlight mysql %}
BOOLEAN ST_Graph(inputTable varchar);
BOOLEAN ST_Graph(inputTable varchar, columnName varchar);
BOOLEAN ST_Graph(inputTable varchar, columnName varchar,
                 tolerance double);
BOOLEAN ST_Graph(inputTable varchar, columnName varchar,
                 tolerance double, orientBySlope boolean);
BOOLEAN ST_Graph(inputTable varchar, columnName varchar,
                 tolerance double, orientBySlope boolean, 
                 deleteTables boolean);                 
{% endhighlight %}

### Description

Produces two tables (nodes and edges) from the geometries contained in column
`columnName` of table `inputTable`. If no column is specified, then the first
Geometry column is used. Returns `true` if the operation is successful.

| Variable        | Default value                   |
|-----------------|---------------------------------|
| `columnName`    | The first geometry column found |
| `tolerance`     | `0.0`                           |
| `orientBySlope` | `false`                         |

If `deleteTables` is equal to `1`, existing tables (with the same prefix `inputTable`) are removed.

<div class="note warning">
  <h5>The column must only contain <code>LINESTRING</code>s.</h5>
  <p>Otherwise, the operation will fail and <code>ST_Graph</code> will return
  <code>false</code>.</p>
</div>

<div class="note warning">
  <h5>The <code>inputTable</code> must contain a Primary Key.</h5>
  <p>Otherwise, the operation will fail.</p>
</div>

<div class="note info">
  <h5>If the input table is named <code>input</code>, then the output tables
  will be named <code>input_nodes</code> and <code>input_edges</code></h5>
  <p>The <code>input_nodes</code> table contains:</p>
  <ul>
  <li>an integer id <code>node_id</code></li>
  <li>a <code>POINT</code> Geometry representing each node</li>
  </ul>
  <p>The <code>input_edges</code> table is a copy of the input table with three
  extra integer id columns:</p>
  <ul>
  <li><code>edge_id</code></li>
  <li><code>start_node</code></li>
  <li><code>end_node</code></li>
  </ul>
  <p>The last two columns correspond to the <code>node_id</code>s in the
  <code>input_nodes</code> table.</p>
</div>

<div class="note">
  <h5>Correct data inaccuracies automatically.</h5>
  <p>When the endpoints of certain <code>LINESTRINGs</code> are very close together, we
  often wish to snap them together. The <code>tolerance</code> value allows us to do that.
  It specifies the side length of a square Envelope around each node used to
  snap together other nodes within the same Envelope. <i>Note</i>:
  <ul>
  <li>Edge geometries are left untouched.</li>
  <li> <i>Coordinates</i> within a given tolerance of each other are not
  necessarily snapped together. Only the first and last coordinates of a
  Geometry are considered to be nodes, and only <i>nodes</i> within a given
  tolerance of each other are snapped together.</li>
  </ul>
  </p>
</div>

<div class="note warning">
  <h5>The tolerance works only in metric units.</h5>
</div>

<div class="note">
  <h5>Hydrologists, watch out!</h5>
  <p>By setting <code>orientBySlope</code> to <code>true</code>, you can
  specify that edges should be oriented from the endpoint with greatest
  <i>z</i>-value to the endpoint with least <i>z</i>-value.</p>
</div>

### Examples

##### First Geometry column detection

{% highlight mysql %}
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                  description VARCHAR, way LINESTRING);
INSERT INTO test VALUES
('1','LINESTRING(0 0, 1 2)', 'road1', 'LINESTRING(1 1, 2 2, 3 1)'),
('2','LINESTRING(1 2, 2 3, 4 3)', 'road2', 'LINESTRING(3 1, 2 0, 1 1)'),
('3','LINESTRING(4 3, 4 4, 1 4, 1 2)', 'road3', 'LINESTRING(1 1, 2 1)'),
('4','LINESTRING(4 3, 5 2)', 'road4', 'LINESTRING(2 1, 3 1)');

-- We first demonstrate automatic Geometry column detection.
-- ST_Graph finds and uses the 'road' column.
SELECT ST_Graph('test');
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |     GEOM    |
--     |---------|-------------|
--     |    1    | POINT(0 0)  |
--     |    2    | POINT(1 2)  |
--     |    3    | POINT(4 3)  |
--     |    4    | POINT(5 2)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |
-- |    2    |     2      |    3     |
-- |    3    |     3      |    2     |
-- |    4    |     3      |    4     |

-- We may also choose which Geometry column we want to use.
-- Here we specify the 'way' column.
DROP TABLE test_nodes;
DROP TABLE test_edges;
SELECT ST_Graph('test', 'way');
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |     GEOM    |
--     |---------|-------------|
--     |    1    | POINT(1 1)  |
--     |    2    | POINT(3 1)  |
--     |    3    | POINT(2 1)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |
-- |    2    |     2      |    1     |
-- |    3    |     1      |    3     |
-- |    4    |     3      |    2     |
{% endhighlight %}

##### Using a tolerance

{% highlight mysql %}
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                                 description VARCHAR);
INSERT INTO test VALUES ('1', 'LINESTRING(0 0, 1 0)', 'road1'),
                        ('2', 'LINESTRING(1.05 0, 2 0)', 'road2'),
                        ('3', 'LINESTRING(2.05 0, 3 0)', 'road3'),
                        ('4', 'LINESTRING(1 0.1, 1 1)', 'road4'),
                        ('5', 'LINESTRING(2 0.05, 2 1)', 'road5');

-- This example shows that coordinates within a tolerance of 0.05 of
-- each other are considered to be a single node. Note, however, that
-- edge geometries are left untouched.
SELECT ST_Graph('test', 'road', 0.05);
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |     GEOM      |
--     |---------|---------------|
--     |    1    | POINT(0 0)    |
--     |    2    | POINT(1.05 0) |
--     |    3    | POINT(2.05 0) |
--     |    4    | POINT(3 0)    |
--     |    5    | POINT(1 1)    |
--     |    6    | POINT(2 1)    |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |
-- |    2    |     2      |    3     |
-- |    3    |     3      |    4     |
-- |    4    |     2      |    5     |
-- |    5    |     3      |    6     |
{% endhighlight %}

##### Orienting by z-values

{% highlight mysql %}
-- This test proves that orientation by slope works. Three cases:
--     1. first.z == last.z -- Orient first --> last
--     2. first.z > last.z -- Orient first --> last
--     3. first.z < last.z -- Orient last --> first

--------------------------------------
-- CASE 1: 0 == 0.
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                  description VARCHAR);
INSERT INTO test VALUES ('1', 'LINESTRING(0 0 0, 1 0 0)', 'road1');
SELECT ST_Graph('test', 'road', 0.0, true);
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |     GEOM      |
--     |---------|---------------|
--     |    1    | POINT(0 0 0)  |
--     |    2    | POINT(1 0 0)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |

--------------------------------------
-- CASE 2: 1 > 0.
DROP TABLE test;
DROP TABLE test_nodes;
DROP TABLE test_edges;
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                  description VARCHAR);
INSERT INTO test VALUES ('1', 'LINESTRING(0 0 1, 1 0 0)', 'road1');
SELECT ST_Graph('test', 'road', 0.0, true);
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |     GEOM      |
--     |---------|---------------|
--     |    1    | POINT(0 0 1)  |
--     |    2    | POINT(1 0 0)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     1      |    2     |

--------------------------------------
-- CASE 3: 0 < 1.
DROP TABLE test;
DROP TABLE test_nodes;
DROP TABLE test_edges;
CREATE TABLE test(pk INTEGER PRIMARY KEY, road LINESTRING, 
                  description VARCHAR);
INSERT INTO test VALUES ('1', 'LINESTRING(0 0 0, 1 0 1)', 'road1');
SELECT ST_Graph('test', 'road', 0.0, true);
-- Answer: TRUE

SELECT * FROM test_nodes;
-- Answer:
--     | NODE_ID |     GEOM      |
--     |---------|---------------|
--     |    1    | POINT(0 0 0)  |
--     |    2    | POINT(1 0 1)  |

SELECT * FROM test_edges;
-- Answer:
-- | EDGE_ID | START_NODE | END_NODE |
-- |---------|------------|----------|
-- |    1    |     2      |    1     |
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/topology/ST_Graph.java" target="_blank">Source code</a>
