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
`columnName` of table `inputTable`. If no column is specified, then the first
Geometry column is used. Returns `true` if the operation is successful.

| Variable | Default value |
| - | - |
| `columnName` | The first geometry column found |
| `tolerance` | `0.0` |
| `orientBySlope` | `false` |

<div class="note warning">
  <h5>The column must only contain <code>LINESTRING</code>s or
  <code>MULTILINESTRING</code>s.</h5>
  <p>Otherwise, the operation will fail and <code>ST_Graph</code> will return
  <code>false</code>.</p>
</div>

<div class="note info">
  <h5>If the input table is named <code>input</code>, then the output tables
  will be named <code>input_nodes</code> and <code>input_edges</code></h5>
  <p>The <code>input_nodes</code> contains:</p>
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

{% highlight mysql %}
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2network/src/main/java/org/h2gis/network/graph_creator/ST_Graph.java" target="_blank">Source code</a>
* Added: <a href="https://github.com/irstv/H2GIS/pull/191" target="_blank">#191</a>
