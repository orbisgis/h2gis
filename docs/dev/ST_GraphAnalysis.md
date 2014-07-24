---
layout: docs
title: ST_GraphAnalysis
category: applications/h2network
is_function: true
description: Calculate closeness/betweenness centrality of vertices and edges
prev_section: ST_Graph
next_section: ST_ShortestPath
permalink: /docs/dev/ST_GraphAnalysis/
---

### Signatures

{% highlight mysql %}
-- Creates two tables:
--     INPUT_EDGES_NODE_CENT[NODE_ID, BETWEENNESS, CLOSENESS]
--     INPUT_EDGES_EDGE_CENT[EDGE_ID, BETWEENNESS]
ST_GraphAnalysis('INPUT_EDGES', 'o[ - eo]');      -- Unweighted
ST_GraphAnalysis('INPUT_EDGES', 'o[ - eo]', 'w'); -- Weighted
{% endhighlight %}

### Description

Uses [Brande's betweenness centrality algorithm][brandes] to
calculate closeness and betweenness [centrality][wiki] for vertices
and betweenness centrality for edges.

<div class="note">
  <h5>All centrality scores are normalized.</h5>
</div>

<div class="note warning">
  <h5>A few caveats.</h5>
  <p> Results will not be accurate if the graph:
  <ul>
  <li> contains "duplicate" edges (having the same source, destination and
  weight)
  </li>
  <li> is disconnected. Use <a
  href="../ST_ConnectedComponents"><code>ST_ConnectedComponents</code></a> to
  make sure you're calling <code>ST_GraphAnalysis</code> on a single (strongly)
  connected component. If all closeness centrality scores are zero, this is
  why.
  </li>
  </ul>
  </p>
</div>

##### Input parameters

| Variable      | Meaning                                                                                                                                                                               |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `INPUT_EDGES` | Edges table produced by `ST_Graph` from table `input`                                                                                                                                 |
| `o`           | Global orientation string: `directed`, `reversed` or `undirected`                                                                                                                     |
| `eo`          | Edge orientation column name indicating individual edge orientations: `1` (directed), `-1` (reversed) or `0` (undirected); required if global orientation is `directed` or `reversed` |
| `w`           | Edge weights column name                                                                                                                                                              |

### Examples

{% highlight mysql %}
{% endhighlight %}

##### See also

* [`ST_ConnectedComponents`](../ST_ConnectedComponents)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_GraphAnalysis.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Centrality
[brandes]: http://www.inf.uni-konstanz.de/algo/publications/b-fabc-01.pdf
