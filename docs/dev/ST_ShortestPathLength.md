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
| `INPUT_EDGES` | Edges table produced by `ST_Graph` from table `input`                                                                                                                                 |
| `o`           | Global orientation string: `directed`, `reversed` or `undirected`                                                                                                                     |
| `eo`          | Edge orientation column name indicating individual edge orientations: `1` (directed), `-1` (reversed) or `0` (undirected); required if global orientation is `directed` or `reversed` |
| `w`           | Edge weights column name                                                                                                                                                              |
| `s`           | Source vertex id                                                                                                                                                                      |
| `d`           | Destination vertex id                                                                                                                                                                 |
| `sdt`         | Source-Destination table name; must contain columns `SOURCE` and `DESTINATION` containing integer vertex ids                                                                          |
| `ds`          | Comma-separated destination string: `'dest1, dest2, ...'`                                                                                                                             |

### Examples

{% highlight mysql %}
{% endhighlight %}

##### See also

* [`ST_ShortestPath`](../ST_ShortestPath),
  [`ST_Accessibility`](../ST_Accessibility)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_ShortestPathLength.java" target="_blank">Source code</a>
