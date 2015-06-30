# H2GIS [![Build Status](https://travis-ci.org/orbisgis/h2gis.png?branch=master)](https://travis-ci.org/orbisgis/h2gis)

H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database
engine in the spirit of [PostGIS](http://postgis.net/). It adds support for
managing spatial features and operations on the new `Geometry` type of H2, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features
for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and
additional spatial functions that we (the [CNRS](http://www.cnrs.fr/))
develop. 

H2GIS is the root project for the new [OrbisGIS](http://www.orbisgis.org/) data
management library. It contains two main subprojects: H2Spatial and
H2Drivers, both of which are licensed under the GPL 3 license terms.

#### H2Spatial
H2Spatial extends H2 by adding spatial storage and analysis capabilities,
including

- a constraint on `Geometry` data type storing `POINT`, `CURVE` and `SURFACE` types in
  WKB representations
- spatial operators (`ST_Intersection`, `ST_Difference`, etc.)
- spatial predicates (`ST_Intersects`, `ST_Contains`, etc.)

#### H2Spatial Extension

Additional spatial SQL functions that are not in [Simple Features for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL)

Ex: `ST_Extent`, `ST_Explode`

#### H2Drivers
H2Drivers add H2 read/write support for file formats such as .shp, .dbf, .geojson, .gpx

This package include 2 implementation of TableEngine that allow you to immediatly 'link' a table with a shape file.

It include also file copy functions:
* SHPREAD( ) and SHPWRITE( ) to read and write Esri shape files.
* DBFREAD( ) and DBFWRITE( ) to read and write DBase III files.
* GeoJsonRead() and GeoJsonWrite() to read and write GeoJSON files.
* GPXRead() to read GPX files.
### Usage

For now, H2GIS requires Java 6. Run `maven clean install -P standalone` in the H2GIS's root directory.

In the folder `h2-dist/target/` you will find a zip file `h2gis-standalone-bin.zip`.Unzip the file then open `h2-dist-1.1.1.jar` It will open a browser based console application.

~ $ unzip h2gis-standalone-bin.zip

~ $ cd h2gis-standalone

~/h2gis-standalone $ java -jar h2-dist-1.1.1.jar

Click Connect in the web interface


[Create a database](http://www.h2database.com/html/quickstart.html) and run the following commands to add spatial features (do it only after the creation of a new database):

```sql
CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR "org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension";
CALL SPATIAL_INIT();
```

You can open a shapefile by calling the following SQL request:

```sql
CALL FILE_TABLE('/home/user/myshapefile.shp', 'tablename');
```
This special table will be immediatly created (no matter the file size). The content will allways be synchronized with the file content.

You can also copy the content of the file into a regular H2 table:

```sql
CALL SHPREAD('/home/user/myshapefile.shp', 'tablename');
```

Or copy the content of a spatial table in a new shape file:

```sql
CALL SHPWRITE('/home/user/newshapefile.shp', 'tablename');
```

#### Contributing

For legal reasons, contributors are asked to provide a contributor license agreement. 
We invite each contributor to send a mail to the [H2GIS developer] (http://h2gis.1099522.n5.nabble.com/H2GIS-developers-f3.html) mailing list.

The mail need to include the following statement:

"I wrote the code, it's mine, and I'm contributing it to H2GIS for distribution licensed under the [GPL 3.0] (http://www.gnu.org/copyleft/gpl.html)." 

For a significant contribution, send a PR on GitHub and refer it in your message. For a single contribution join a patch to your mail.


#### Acknowledgements

The H2GIS team utilizes open source software. Specifically, we would like to thank  :

* Thomas Mueller and Noel Grandin from the [H2 database community] (http://www.h2database.com).
* Martin Davis from the [JTS community] (http://tsusiatsoftware.net/jts/main.html).

#### Supporters

Many thanks for those who reported bugs or provide patches...  

* Steve Hruda aka shruda [PR #453] (https://github.com/irstv/H2GIS/pull/453)
* Ivo Šmíd aka bedla [PR #556] (https://github.com/orbisgis/h2gis/pull/556)


#### Team

H2GIS is composed of three qualified professionals in GIS and informatic sciences.
Erwan Bocher leads the project.
Nicolas Fortin is the lead programmer. 
Gwendall Petit is in charge of the documentation and manages all public relations with the community users.


