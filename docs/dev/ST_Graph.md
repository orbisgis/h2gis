---
layout: docs
title: ST_Graph
category: h2network/graph-creation
description: Produce nodes and edges tables from an input table containing <code>(MULTI)LINESTRINGS</code>
prev_section: h2network/graph-creation
next_section:
permalink: /docs/dev/ST_Graph/
---

### Signatures

{% highlight mysql %}
boolean ST_Graph(inputTable varchar);
boolean ST_Graph(inputTable varchar, columnName varchar);
boolean ST_Graph(inputTable varchar, columnName varchar,
                 tolerance double);
boolean ST_Graph(inputTable varchar, columnName varchar,
                 tolerance double, orientBySlope boolean);
{% endhighlight %}

### Description

Produces two tables (nodes and edges) from the geometries contained in column
`columnName` of `inputTable` using the specified `tolerance` and orienting
edges by slope if `orientBySlope` is true. Returns `true` if the operation
is successful.

| Variable | Default value |
| - | - |
| `columnName` | The first geometry column found |
| `tolerance` | `0.0` |
| `orientBySlope` | `false` |

<div class="note">
  <h5>If the input table is named <code>input</code>, then the output tables
  will be named <code>input_nodes</code> and <code>input_edges</code>.</h5>
  <p>The nodes table consists of an integer <code>node_id</code> and a
  <code>POINT</code> geometry representing each node. The edges table is a copy
  of the input table with three extra columns: <code>edge_id,</code>
  <code>start_node,</code> and <code>end_node.</code> The
  <code>start_node</code> and <code>end_node</code> correspond to the
  <code>node_id</code>s in the nodes table.</p>
</div>

<div class="note warning">
  <h5>The column can only contain <code>LINESTRING</code>s or <code>MULTILINESTRING</code>s.</h5>
  <p>Otherwise, the operation will fail and <code>ST_Graph</code> will return <code>false</code>.</p>
</div>

A tolerance value may be given to specify the side length of a square Envelope
around each node used to snap together other nodes within the same Envelope.
Note, however, that edge geometries are left untouched.  Note also that
coordinates within a given tolerance of each other are not necessarily snapped
together. Only the first and last coordinates of a geometry are considered to
be potential nodes, and only nodes within a given tolerance of each other are
snapped together. The tolerance works only in metric units.

A boolean value may be set to true to specify that edges should be oriented the
z-value of their first and last coordinates (decreasing).

### Examples

{% highlight mysql %}
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_Graph.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/191" target="_blank">#191</a>
