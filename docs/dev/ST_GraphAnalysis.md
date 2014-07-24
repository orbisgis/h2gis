---
layout: docs
title: ST_GraphAnalysis
category: applications/h2network
is_function: true
is_math: true
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
ST_GraphAnalysis('INPUT_EDGES', 'o[ - eo]'[, 'w']);
{% endhighlight %}

### Description

Uses [Brande's betweenness centrality algorithm][brandes] to
calculate closeness and betweenness [centrality][wiki] for vertices
and betweenness centrality for edges.

Let $$d(s, t)$$ denote the **distance** from $$s \in V$$ to $$t \in
V$$, i.e., the minimum length of all paths connecting $$s$$ to
$$t$$. We have $$d(s, s) = 0$$ for all $$s \in V$$.

Let $$\sigma_{st}$$ denote the number of shortest paths from $$s \in
V$$ to $$t \in V$$ and set $$\sigma_{ss}=1$$ by convention. Let
$$\sigma_{st}(v)$$ denote the number of shortest paths from $$s$$ to
$$t$$ containing $$v \in V$$.

We have the following definitions for vertices:
<div>
\begin{array}{l r}
    C_C(v) = \left(\sum_{t \in V} d(v, t)\right)^{-1}
    & \qquad \textrm{closeness centrality} \\
    C_B(v) = \sum_{s \neq t \neq v \in V} \frac{\sigma_{st}(v)}{\sigma_{st}}
    & \qquad \textrm{betweenness centrality} \\
\end{array}
</div>

Betweenness centrality for edges is defined similarly.

A high closeness centrality score indicates that a vertex can reach
other vertices on relatively short paths; a high betweenness
centrality score indicates that a vertex lies on a relatively high
number of shortest paths.

<div class="note">
  <h5>All centrality scores are normalized.</h5>
  <p>But this normalization depends on the graph being connected.
  Use <a
  href="../ST_ConnectedComponents"><code>ST_ConnectedComponents</code></a>
  to make sure you're calling <code>ST_GraphAnalysis</code> on a
  single (strongly) connected component.</p>
</div>

<div class="note warning">
  <h5>A few caveats.</h5>
  <p> Results will not be accurate if the graph:
  <ul>
  <li> contains "duplicate" edges (having the same source,
  destination and weight)
  </li>
  <li> is disconnected. If all closeness centrality scores are zero,
  this is why.
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

##### Screenshots

Closeness centrality of Nantes, France. Notice the limited traffic
zone in the center.

<img class="displayed" src="../nantes-closeness-cropped.png">

Edge betweenness centrality of Nantes, France. Notice how the
beltway and bridges really stand out.

<img class="displayed" src="../nantes-betweenness-cropped.png">

The above screenshots were generated in [OrbisGIS][og].

### Examples

{% highlight mysql %}
{% endhighlight %}

##### See also

* [`ST_ConnectedComponents`](../ST_ConnectedComponents)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_GraphAnalysis.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Centrality
[brandes]: http://www.inf.uni-konstanz.de/algo/publications/b-fabc-01.pdf
[og]: http://www.orbisgis.org
