# H2GIS
[![GitHub](https://img.shields.io/github/license/orbisgis/h2gis.svg)](https://github.com/orbisgis/h2gis/blob/master/LICENSE.md) 
[![Build Stat](https://img.shields.io/jenkins/s/http/jenkins-ng.orbisgis.org/job/h2gis.svg)](http://jenkins-ng.orbisgis.org/job/h2gis) 
[![Build Test](https://img.shields.io/jenkins/t/http/jenkins-ng.orbisgis.org/job/h2gis.svg)](https://jenkins-ng.orbisgis.org/job/h2gis/test_results_analyzer/) 
[![codecov](https://img.shields.io/codecov/c/github/orbisgis/h2gis.svg)](https://codecov.io/gh/orbisgis/h2gis) 
[![Codacy Badge](https://img.shields.io/codacy/grade/93899ea0675d43a2a3787ce5dd3c5595.svg)](https://www.codacy.com/app/orbisgis/h2gis?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=orbisgis/h2gis&amp;utm_campaign=Badge_Grade) 


H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database
engine in the spirit of [PostGIS](http://postgis.net/). It adds support for
managing spatial features and operations on the new `Geometry` type of H2, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features
for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and
additional spatial functions that we (the [CNRS](http://www.cnrs.fr/))
develop. 

H2GIS is the root project for the new [OrbisGIS](http://www.orbisgis.org/) data
management library. It contains tools to execute geometry analysis and read/write geospatial file formats.

H2GIS is licensed under the LGPL 3 license terms.

#### h2gis-functions
h2gis-functions is the main module of the H2GIS distribution. 
It extends H2 by adding spatial storage and analysis capabilities,including
- a constraint on `Geometry` data type storing `POINT`, `CURVE` and `SURFACE` types in WKB representations
- spatial operators (`ST_Intersection`, `ST_Difference`, etc.)
- spatial predicates (`ST_Intersects`, `ST_Contains`, etc.)
- additional spatial SQL functions that are not in [Simple Features for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL)

Ex: `ST_Extent`, `ST_Explode`, `ST_MakeGrid`

It contains a set of driver functions (I/O)) to read/write file formats such as .shp, .dbf, .geojson, .gpx

This I/O package include 2 implementation of TableEngine that allow you to immediatly 'link' a table with a shape file.

It include also file copy functions (import):
* SHPREAD( ) and SHPWRITE( ) to read and write Esri shape files.
* DBFREAD( ) and DBFWRITE( ) to read and write DBase III files.
* GeoJsonRead() and GeoJsonWrite() to read and write GeoJSON files.
* GPXRead() to read GPX files.

### Usage

For now, H2GIS requires Java 6. Run `maven clean install -P standalone` in the H2GIS's root directory.

In the folder `h2gis-dist/target/` you will find a zip file `h2gis-standalone-bin.zip`.Unzip the file then open `h2gis-dist-xxx.jar` It will open a browser based console application.

~ $ unzip h2gis-standalone-bin.zip

~ $ cd h2gis-standalone

~/h2gis-standalone $ java -jar h2gis-dist-xxx.jar

Click Connect in the web interface


[Create a database](http://www.h2database.com/html/quickstart.html) and run the following commands to add spatial features (do it only after the creation of a new database):

#### Initialize the H2GIS extension

To initialize the H2GIS extension apply the SQL syntax:

```sql
CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR "org.h2gis.functions.factory.H2GISFunctions.load";
CALL H2GIS_SPATIAL();
```

When the functions are installed you can open a shapefile by calling the following SQL request:

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
We invite each contributor to send a mail to the [H2GIS developer](http://h2gis.1099522.n5.nabble.com/H2GIS-developers-f3.html) mailing list.

The mail need to include the following statement:

"I wrote the code, it's mine, and I'm contributing it to H2GIS for distribution licensed under the [LGPL 3.0](http://www.gnu.org/copyleft/lgpl.html)." 

For a significant contribution, send a PR on GitHub and refer it in your message. For a single contribution join a patch to your mail.


#### Download

To download the last H2GIS stable release and find documentation please go to [http://www.h2gis.org](http://www.h2gis.org)

You can include H2GIS in your project thanks to Maven repositories.

From maven central, check https://search.maven.org/artifact/org.orbisgis/h2gis/1.4.0/bundle

To use the current snapshot add in the pom
```xml
<repository>
  <id>orbisgis-nexus-snapshot</id>
  <name>OrbisGIS nexus snapshot repository</name>
  <url>http://nexus.orbisgis.org/content/repositories/osgi-maven-snapshot</url>
</repository>
```

#### Acknowledgements

The H2GIS team utilizes open source software. Specifically, we would like to thank  :

* Thomas Mueller and Noel Grandin from the [H2 database community](http://www.h2database.com).
* Martin Davis from the [JTS Topology Suite community](https://github.com/locationtech/jts).

#### Supporters

Many thanks for those who reported bugs or provide patches...  

* Steve Hruda aka shruda [PR #453](https://github.com/irstv/H2GIS/pull/453)
* Ivo Šmíd aka bedla [PR #556](https://github.com/orbisgis/h2gis/pull/556), [PR #695](https://github.com/orbisgis/h2gis/pull/695)
* Michaël Michaud [PR #778](https://github.com/orbisgis/h2gis/pull/778)


#### Team

H2GIS is composed of four qualified professionals in GIS and informatic sciences.
* Erwan Bocher leads the project.
* Nicolas Fortin is the lead programmer. 
* Sylvain Palominos is the lead programmer of the OrbisGIS platform.
* Gwendall Petit is in charge of the documentation and manages all public relations with the community users.



