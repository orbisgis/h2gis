H2GIS
=====

H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database
engine in the spirit of [PostGIS](http://postgis.net/). It adds support for
managing spatial features and operations including a `Geometry` type, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features
for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and
additional spatial functions that we (the [Atelier SIG](http://www.irstv.fr/))
develop. 

H2GIS is the root project for the new [OrbisGIS](http://www.orbisgis.org/) data
management library and is divided into two subprojects: H2Spatial and
H2Drivers.

### H2Spatial
H2Spatial extends H2 by adding spatial storage and analysis capabilities,
including

- a constraint on `Geometry` data type storing `POINT`, `CURVE` and `SURFACE` types in
  WKB representations
- spatial operators (`ST_Intersection`, `ST_Difference`, etc.)
- spatial predicates (`ST_Intersects`, `ST_Contains`, etc.)
### H2Spatial Extension

Additional spatial SQL functions that are not in [Simple Features for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL)

Ex: ST_Extent, ST_Explode

### H2Drivers
H2Drivers makes use of the TableEngine API to add H2 read/write support for file
formats such as .shp,.dbf and .mif. With H2Drivers, the user can execute SQL queries
on file formats other than pure H2.

Both H2Spatial and H2Drivers are licensed under the GPL 3 license terms.
           
# How to Use

Build the project at the root by running:

```bsh
mvn clean install
```
Then go to the folder h2-dist and run:
```bsh
mvn package assembly:single
```

In the target folder you will find a zip file that contain a run.sh file in order to run H2 with H2GIS.

[Create a DataBase](http://www.h2database.com/html/quickstart.html) and run the following commands to add spatial features:

```sql
CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR "org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension";
CALL SPATIAL_INIT();
```

You can open a Shape file by calling the following SQL request:

```sql
CALL FILE_TABLE('/home/user/myshapefile.shp','tablename');
```