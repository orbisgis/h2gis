# H2GIS [![Build Status](https://travis-ci.org/irstv/H2GIS.png?branch=master)](https://travis-ci.org/irstv/H2GIS)

H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database
engine in the spirit of [PostGIS](http://postgis.net/). It adds support for
managing spatial features and operations on the new `Geometry` type of H2, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features
for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and
additional spatial functions that we (the [Atelier SIG](http://www.irstv.fr/))
develop. There is currently about 70 spatial functions in H2GIS. 

H2GIS is the root project for the new [OrbisGIS](http://www.orbisgis.org/) data
management library and is divided into two subprojects: H2Spatial and
H2Drivers, both of which are licensed under the GPL 3 license terms.

#### H2Spatial
H2Spatial extends H2 by adding spatial storage and analysis capabilities,
including

- a constraint on `Geometry` data type storing `POINT`, `CURVE` and `SURFACE` types in
  WKB representations
- spatial operators (`ST_Intersection`, `ST_Difference`, etc.)
- spatial predicates (`ST_Intersects`, `ST_Contains`, etc.)
### H2Spatial Extension

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

In the folder `h2-dist/target/` you will find a zip file `h2gis-standalone-bin.zip` that contain a run.sh file for running H2 with H2GIS. It will open a browser based console application.

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

#### Acknowledgements

The H2GIS team utilizes open source software. Specifically, we would like to thank  :

* Thomas Mueller and Noel Grandin from [H2 database community] (http://www.h2database.com),
* Martin Davis from [JTS community] (http://tsusiatsoftware.net/jts/main.html),



