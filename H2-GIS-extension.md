H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database
engine in the spirit of [PostGIS](http://postgis.net/). It adds support for
managing spatial features and operations including `(M)Polygon`,`(M)LineString`,`(M)Point` type, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features
for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and
additional spatial functions that we (the [Atelier SIG](http://www.irstv.fr/))
develop. 

# Quick Start
Download the [last binary package](http://jenkins.orbisgis.org/view/H2%20GIS/job/H2GIS%20Deploy/lastBuild/org.h2gis$h2-dist/) h2-dist-###-bin.zip

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