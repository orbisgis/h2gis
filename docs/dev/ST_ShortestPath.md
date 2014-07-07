---
layout: docs
title: ST_ShortestPath
category: applications/h2network
is_function: true
description: Calculate shortest path(s) between vertices in a graph
prev_section: ST_GraphAnalysis
next_section: ST_ShortestPathLength
permalink: /docs/dev/ST_ShortestPath/
---

### Signatures

{% highlight mysql %}
-- Return type:
--     TABLE[THE_GEOM, EDGE_ID, PATH_ID, PATH_EDGE_ID,
--           SOURCE, DESTINATION, WEIGHT]
-- One-to-One
ST_ShortestPath('INPUT_EDGES', 'o[ - eo]', s, d);
-- One-to-One Weighted
ST_ShortestPath('INPUT_EDGES', 'o[ - eo]', 'w', s, d);
{% endhighlight %}

### Description

Calculates the shortest path(s) from source vertex `s` to
destination vertex `d`.
Multiple shortest paths are distinguished by `PATH_ID`.
`EDGE_ID` indicates the ID in `INPUT_EDGES` while `PATH_EDGE_ID` is
a new ID for this path.

##### Input parameters

| Variable      | Meaning                                                                                                                                                                               |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `INPUT_EDGES` | Edges table produced by `ST_Graph` from table `input`                                                                                                                                 |
| `o`           | Global orientation string: `directed`, `reversed` or `undirected`                                                                                                                     |
| `eo`          | Edge orientation column name indicating individual edge orientations: `1` (directed), `-1` (reversed) or `0` (undirected); required if global orientation is `directed` or `reversed` |
| `w`           | Edge weights column name                                                                                                                                                              |
| `s`           | Source vertex id                                                                                                                                                                      |
| `d`           | Destination vertex id                                                                                                                                                                 |

### Examples

{% highlight mysql %}
{% endhighlight %}

##### See also

* [`ST_ShortestPathLength`](../ST_ShortestPathLength)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_ShortestPath.java" target="_blank">Source code</a>
