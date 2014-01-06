---

layout: docs

title: Welcome

permalink: /docs/dev/home/

---

H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database
engine in the spirit of [PostGIS](http://postgis.net/). It adds support for
managing spatial features and operations including `(M)Polygon`, `(M)LineString` and `(M)Point` types, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features
for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and
additional spatial functions that we (the [Atelier SIG](http://www.irstv.fr/))
develop. 

# Quick Start
Download H2GIS standalone jar [latest binary package](http://jenkins.orbisgis.org/job/H2GIS-Deploy/lastSuccessfulBuild/artifact/h2-dist/target/h2gis-standalone-bin.zip)

Unzip and run the jar by clicking on it or using the run.sh.
Find the h2 sql client on https://localhost:8082.
Click on `Connect` to open a test database located on your user folder.

To initialize spatial capabilities:

{% highlight sql %}
CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR
    "org.h2gis.h2spatialext.CreateSpatialExtension
        .initSpatialExtension";
CALL SPATIAL_INIT();
{% endhighlight %}

You can open a shape file:

{% highlight sql %}
CALL FILE_TABLE('/home/user/myshapefile.shp','tablename');
{% endhighlight %}

You can then show the content:
{% highlight sql %}
SELECT * FROM TABLENAME;
{% endhighlight %}

## Function documentation

Function documentation may be found here:

* [[h2spatial-ext | Function documentation: h2spatial ext]]

## Spatial Index
On regular tables (not shapes) you can add a spatial index (stored on disk):
{% highlight sql %}
CREATE TABLE area(idarea int PRIMARY KEY, the_geom GEOMETRY);
CREATE SPATIAL INDEX myspatialindex ON area(the_geom);
INSERT INTO area VALUES(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))');
INSERT INTO area VALUES(2, 'POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))');
CREATE TABLE ROADS(idroad int PRIMARY KEY, the_geom GEOMETRY);
CREATE SPATIAL INDEX ON roads(the_geom);
INSERT INTO roads VALUES(1, 'LINESTRING (27.65595463138 -16.728733459357244, 47.61814744801515 40.435727788279806)');
INSERT INTO roads VALUES(2, 'LINESTRING (17.674858223062415 55.861058601134246, 55.78449905482046 76.73062381852554)');
{% endhighlight %}

The spatial predicate operator `&&` for bounding box overlap uses this index:
{% highlight sql %}
SELECT idarea, COUNT(idroad) roadscount
    FROM area,roads
    WHERE area.the_geom && roads.the_geom
    AND ST_Intersects(area.the_geom,roads.the_geom)
    GROUP BY idarea
    ORDER BY idarea
{% endhighlight %}

## Spatial JDBC

One of H2GIS's goals is to provide a common interface to H2 and PostGIS for Geometry data. The `spatial-utilities` package provides a **DataSource** and **Connection** wrapper in order to facilitate the usage of JDBC with Geometry fields.

### Usage

When acquiring the **DataSource** or the **Connection**, wrap it through `SFSUtilities.wrapSpatialDataSource `or `SFSUtilities.wrapSpatialConnection`.

{% highlight java %}
import org.osgi.service.jdbc.DataSourceFactory;
public DataSource getDataSource(DataSourceFactory dataSourceFactory) {
    dataSource = SFSUtilities.wrapSpatialDataSource(
        dataSourceFactory.createDataSource(properties));
}
{% endhighlight %}

Then when you get a `ResultSet` through a spatial table you can use the following command:
{% highlight java %}
private void doStuff(Statement st) {
    SpatialResultSet rs = st.executeQuery(
        "SELECT the_geom FROM mygeomtable")
            .unWrap(SpatialResultSet.class);
    rs.next();
    Geometry myGeom = rs.getGeometry("the_geom");
}
{% endhighlight %}

## Custom function aliases

You can define java functions in sql.

{% highlight sql %}
CREATE ALIAS PRINT AS $$ void print(String s) { System.out.println(s); } $$;
{% endhighlight %}

## H2GIS as an embedded spatial database

You can find a short sample that creates an embedded spatial database
[here](https://github.com/irstv/orbisgis-samples/tree/master/demoh2gis).
