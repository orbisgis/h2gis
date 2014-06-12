---
layout: docs
title: ST_Accessibility
category: h2network/graph-functions
description: 
prev_section: h2network/graph-functions
next_section: ST_Graph
permalink: /docs/dev/ST_Accessibility/
---

### Signature

{% highlight mysql %}
tableName[source, closest_dest, distance] ST_Accessibility(
   varchar inputTable, varchar orientation, varchar destination)
tableName[source, closest_dest, distance] ST_Accessibility(
   varchar inputTable, varchar orientation, varchar weight, 
   varchar destination)
{% endhighlight %}

### Description
Calculates, for each vertex in a graph, the closest destination
among several possible destinations as well as the distance to this
destination.

|    Variable   |                                                                                                 Default value                                                                                                 ||

| ------------- | --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `inputTable`  | Edges table produced by `ST_Graph` from table input                               |                                                                                                                                |
| `orientation` | composed by o and eo. `o` =Global orientation (directed, reversed or undirected). | `eo` = Edge orientation (1 = directed, -1 = reversed, 0 = undirected). Required if global orientation is directed or reversed. |
| `weight`      | Name of column containing edge weights as doubles                                 |                                                                                                                                |
| `destination` | `ds` = Comma-separated Destination string ('dest1, dest2, ...')                          | `dt` = Destination table name (must contain column named destination with integer vertex ids)                                         |

Possible signatures: 

* SELECT * FROM ST_Accessibility('input_edges', 'o[ - eo]', 'ds')
* SELECT * FROM ST_Accessibility('input_edges', 'o[ - eo]', 'dt')
* SELECT * FROM ST_Accessibility('input_edges', 'o[ - eo]', 'w', 'ds')
* SELECT * FROM ST_Accessibility('input_edges', 'o[ - eo]', 'w', 'dt')

### Examples

{% highlight mysql %}
CREATE TABLE test(road LINESTRING, GID serial, description VARCHAR,
                  direction int);
INSERT INTO test VALUES
('LINESTRING (0 0, 1 2)', null, 'road1', 1),
('LINESTRING (1 2, 2 3, 4 3)', null,'road2', 0),
('LINESTRING (4 3, 4 4, 1 4, 1 2)', null,'road3', -1),
('LINESTRING (4 3, 5 2)', null,'road4', 0);

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
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/d3c753049f80b83ab85271508550bf92365fb314/h2network/src/main/java/org/h2gis/network/graph_creator/ST_Accessibility.java" target="_blank">Source code</a>
