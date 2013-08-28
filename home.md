H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database
engine in the spirit of [PostGIS](http://postgis.net/). It adds support for
managing spatial features and operations including `(M)Polygon`, `(M)LineString` and `(M)Point` types, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features
for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and
additional spatial functions that we (the [Atelier SIG](http://www.irstv.fr/))
develop. 

# Quick Start
Download the [last binary package](http://jenkins.orbisgis.org/job/H2GIS-Deploy/lastBuild/org.h2gis$h2-dist/) h2-dist-###-bin.zip

Unzip and run the jar by clicking on it or using the run.sh.
You fill find h2 sql client on https://localhost:8082
Click on `Connect` to open a test database located on your user folder.

To init spatial capabilities run the following SQL request:

```sql
CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR "org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension";
CALL SPATIAL_INIT();
```

You can open a Shape file by calling the following SQL request:

```sql
CALL FILE_TABLE('/home/user/myshapefile.shp','tablename');
```

You can then show the content:
```sql
select * from tablename;
```

## Spatial Index
On regular table (not shapes) you can add a spatial index (stored on disk):
```sql
create table area(idarea int primary key, the_geom geometry);
create spatial index myspatialindex on area(the_geom);
insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))');
insert into area values(2, 'POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))');
create table roads(idroad int primary key, the_geom geometry);
create spatial index on roads(the_geom);
insert into roads values(1, 'LINESTRING (27.65595463138 -16.728733459357244, 47.61814744801515 40.435727788279806)');
insert into roads values(2, 'LINESTRING (17.674858223062415 55.861058601134246, 55.78449905482046 76.73062381852554)');
```

The spatial predicate operator `&&` for bounding box overlap use this index:
```sql
select idarea, COUNT(idroad) roadscount from area,roads where area.the_geom && roads.the_geom AND ST_Intersects(area.the_geom,roads.the_geom) GROUP BY idarea ORDER BY idarea
```

## Spatial JDBC

One of the H2GIS goal is to provide a common interface to H2 and PostGIS for Geometry data. The `spatial-utilities` package provide a **DataSource** and **Connection** wrapper in order to facilitate the usage of JDBC with Geometry fields.

### How to use

When acquiring the **DataSource** or the **Connection** wrap it through SFSUtilities.wrapSpatialDataSource or SFSUtilities.wrapSpatialConnection.

```java
import org.osgi.service.jdbc.DataSourceFactory;
public DataSource getDataSource(DataSourceFactory dataSourceFactory) {
    dataSource = SFSUtilities.wrapSpatialDataSource(dataSourceFactory.createDataSource(properties));
}
```

Then when you get a ResultSet trough a spatial table you can use the following command:
```java
private void doStuff(Statement st) {
    SpatialResultSet rs = st.executeQuery("select the_geom from mygeomtable").unWrap(SpatialResultSet.class);
    rs.next();
    Geometry myGeom = rs.getGeometry("the_geom");
}
``` 