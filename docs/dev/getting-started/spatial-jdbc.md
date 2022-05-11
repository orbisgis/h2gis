---
layout: docs
title: Spatial JDBC
prev_section: spatial-indices
next_section: function-aliases
permalink: /docs/dev/spatial-jdbc/
---

One of H2GIS's big-picture goals is to provide a common interface to H2 and
PostGIS for Geometry data.

For convenience, the `spatial-utilities` package provides wrappers that
facilitate using the [Java Database Connectivity][] (JDBC) API in the presence
of Geometry fields.

## Example

`DataSource`s and `Connection`s must be wrapped:

{% highlight java %}
DataSource wrappedDataSource =
    SFSUtilities.wrapSpatialDataSource(originalDataSource);
Connection wrappedConnection
    SFSUtilities.wrapSpatialConnection(originalConnection);
{% endhighlight %}

`ResultSet`s must be unwrapped into `SpatialResultSet`s in order to access
Geometry fields:

{% highlight java %}
SpatialResultSet rs = st.executeQuery(
    "SELECT geom FROM mygeomtable").
        unWrap(SpatialResultSet.class);
rs.next();
Geometry myGeom = rs.getGeometry("geom");
{% endhighlight %}

[Java Database Connectivity]: http://www.oracle.com/technetwork/java/javase/jdbc/index.html
