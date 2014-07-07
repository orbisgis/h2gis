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

### Examples

{% highlight mysql %}
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_ConnectedComponents.java" target="_blank">Source code</a>

[cc]: http://en.wikipedia.org/wiki/Connected_component_(graph_theory)
[scc]: http://en.wikipedia.org/wiki/Strongly_connected_component
