---
layout: docs
title: ST_Accessibility
category: h2network/graph-functions
description: Calculate the closest destination among several possible destinations as well as the distance to this destination.
prev_section: h2network/graph-functions
next_section: ST_ConnectedComponents
permalink: /docs/dev/ST_Accessibility/
---

### Signature

{% highlight mysql %}
tableName[source, closest_dest, distance] ST_Accessibility(
   varchar inputTable, varchar orientation, varchar destination);
tableName[source, closest_dest, distance] ST_Accessibility(
   varchar inputTable, varchar orientation, varchar weight, 
   varchar destination);
{% endhighlight %}

### Description
Calculates, for each vertex in a graph, the closest destination
among several possible destinations as well as the distance to this
destination.

|    Variable   |                                                                              Default value                                                                              |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `inputTable`  | Edges table produced by `ST_Graph` from table input                                                                                                                     |
| `orientation` | Composed by Global orientation `o` and edge orientation `eo`. Global orientation is directed, reversed or undirected.                                                   |
|               | Edge orientation is a column name which contains the orientation (1 = directed, -1 = reversed, 0 = undirected). Required if global orientation is directed or reversed. |
| `weight`      | Name of column containing edge weights as doubles                                                                                                                       |
| `destination` | Two possibility exists: `ds` = Comma-separated Destination string ('dest1, dest2, ...'). The destination string sould be a node_id.                                     |
|               | Or `dt` = Destination table name (must contain column named destination with integer vertex ids)                                                                        |

### Examples

{% highlight mysql %}
CREATE TABLE test(road LINESTRING, GID serial, description VARCHAR,
                  direction int, length double);
INSERT INTO test VALUES
('LINESTRING (0 0, 1 2)', null, 'road1', 1, null),
('LINESTRING (1 2, 2 3, 4 3)', null,'road2', 0, null),
('LINESTRING (4 3, 4 4, 1 4, 1 2)', null,'road3', -1, null),
('LINESTRING (4 3, 5 2)', null,'road4', 0, null);
UPDATE test set length=ST_Length(road);

SELECT ST_Graph('TEST', 'road', 0.1);
CREATE TABLE TEST_EDGES1 AS SELECT a.*, b.* FROM test AS a, 
   test_edges AS b WHERE a.gid=b.edge_id;

SELECT * FROM ST_Accessibility('TEST_EDGES1', 'directed - 
                               direction', '1, 4');
-- Answer: 
-- | source | closest_dest | distance |
-- | ------ | ------------ | -------- |
-- |      1 |            1 |      0.0 |
-- |      2 |            4 |      2.0 |
-- |      3 |            4 |      1.0 |
-- |      4 |            4 |      0.0 |

SELECT * FROM ST_Accessibility('TEST_EDGES1', 'undirected', 
                               '1, 4');
-- Answer: 
-- | source | closest_dest | distance |
-- | ------ | ------------ | -------- |
-- |      2 |            1 |        1 |
-- |      1 |            1 |        0 |
-- |      3 |            4 |        1 |
-- |      4 |            4 |        0 |
{% endhighlight %}

##### Using a weight

{% highlight mysql %}
SELECT * FROM ST_Accessibility('TEST_EDGES1', 'reversed - 
                               direction', 'length', '1, 4');
-- Answer: 
-- | source | closest_dest |      distance      |
-- | ------ | ------------ | ------------------ |
-- |      2 |            1 | 2,23606797749979   |
-- |      1 |            1 | 0                  |
-- |      3 |            4 | 1,4142135623730951 |
-- |      4 |            4 | 0                  |
{% endhighlight %}

##### Using a destination table

{% highlight mysql %}
CREATE TABLE dest(the_geom POINT, destination int);
INSERT INTO test VALUES('POINT (4 3)', 3);

SELECT * FROM ST_Accessibility('TEST_EDGES1', 'reversed - 
                               direction', 'dest');
-- Answer: 
-- | source | closest_dest | distance |
-- | ------ | ------------ | -------- |
-- |      2 |            3 | 1        |
-- |      1 |           -1 | ∞        |
-- |      3 |            3 | 0        |
-- |      4 |            3 | 1        |

-- Note: When a destination is not accessible ST_Accessibility
returns -1 in closest_dest and ∞ in distance.

SELECT * FROM ST_Accessibility('TEST_EDGES1', 'directed - 
                               direction', 'length', 'dest');
-- Answer: 
-- | source | closest_dest |      distance      |
-- | ------ | ------------ | ------------------ |
-- |      1 |            3 | 5,650281539872885  |
-- |      2 |            3 | 3,414213562373095  |
-- |      3 |            3 | 0                  |
-- |      4 |            3 | 1,4142135623730951 |
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/d3c753049f80b83ab85271508550bf92365fb314/h2network/src/main/java/org/h2gis/network/graph_creator/ST_Accessibility.java" target="_blank">Source code</a>
