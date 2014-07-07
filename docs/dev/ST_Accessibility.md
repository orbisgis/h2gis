---
layout: docs
title: ST_Accessibility
category: applications/h2network
is_function: true
description: Calculate, from each vertex, the (distance to the) closest destination
prev_section: applications/h2network
next_section: ST_ConnectedComponents
permalink: /docs/dev/ST_Accessibility/
---

### Signatures

{% highlight mysql %}
-- Return type:
--     TABLE[SOURCE, CLOSEST_DEST, DISTANCE]
ST_Accessibility('INPUT_EDGES', 'o[ - eo]', 'ds');
ST_Accessibility('INPUT_EDGES', 'o[ - eo]', 'dt');
ST_Accessibility('INPUT_EDGES', 'o[ - eo]', 'w', 'ds');
ST_Accessibility('INPUT_EDGES', 'o[ - eo]', 'w', 'dt');
{% endhighlight %}

### Description

Calculates, for each vertex in a graph, the closest destination
among several possible destinations as well as the distance to this
destination.

<div class="note">
  <h5>Using this function will be faster than doing an equivalent
  calculation using <a
  href="../ST_ShortestPathLength"><code>ST_ShortestPathLength</code></a>.</h5>
  <p><code>ST_Accessibility</code> is implemented as follows: The
  graph is reversed, and Dijkstra's algorithm is run from each
  destination vertex. This is much more efficient than running
  Dijkstra's algorithm from each vertex to each destination and
  taking the minimum distance.</p>
</div>

##### Input parameters

| Variable      | Meaning                                                                                                                                                                               |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `INPUT_EDGES` | Edges table produced by `ST_Graph` from table `input`                                                                                                                                 |
| `o`           | Global orientation string: `directed`, `reversed` or `undirected`                                                                                                                     |
| `eo`          | Edge orientation column name indicating individual edge orientations: `1` (directed), `-1` (reversed) or `0` (undirected); required if global orientation is `directed` or `reversed` |
| `w`           | Edge weights column name                                                                                                                                                              |
| `ds`          | Comma-separated destination string: `'dest1, dest2, ...'`                                                                                                                             |
| `dt`          | Destination table name; must contain column `DESTINATION` containing integer vertex ids                                                                                               |

### Examples

{% highlight mysql %}
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_Accessibility.java" target="_blank">Source code</a>
