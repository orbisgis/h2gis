---

layout: docs

title: Spatial JDBC

prev_section: dev/spatial-indices

next_section: dev/function-aliases

permalink: /docs/dev/spatial-jdbc/

---

One of H2GIS's goals is to provide a common interface to H2 and PostGIS for
Geometry data. The `spatial-utilities` package provides a `DataSource` and
`Connection` wrapper in order to facilitate the usage of JDBC with Geometry
fields.

When acquiring the `DataSource` or the `Connection`, wrap it through
`SFSUtilities.wrapSpatialDataSource `or `SFSUtilities.wrapSpatialConnection`.

{% highlight java %}
import org.osgi.service.jdbc.DataSourceFactory;
public DataSource getDataSource(DataSourceFactory dataSourceFactory) {
    dataSource = SFSUtilities.wrapSpatialDataSource(
        dataSourceFactory.createDataSource(properties));
}
{% endhighlight %}

Then when you get a `ResultSet` through a spatial table you can use the
following command:

{% highlight java %}
private void doStuff(Statement st) {
    SpatialResultSet rs = st.executeQuery(
        "SELECT the_geom FROM mygeomtable")
            .unWrap(SpatialResultSet.class);
    rs.next();
    Geometry myGeom = rs.getGeometry("the_geom");
}
{% endhighlight %}
