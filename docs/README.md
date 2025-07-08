# H2GIS
[![GitHub](https://img.shields.io/github/license/orbisgis/h2gis.svg)](https://github.com/orbisgis/h2gis/blob/master/LICENSE.md) 
[![codecov](https://img.shields.io/codecov/c/github/orbisgis/h2gis.svg)](https://codecov.io/gh/orbisgis/h2gis) 
[![Codacy Badge](https://img.shields.io/codacy/grade/93899ea0675d43a2a3787ce5dd3c5595.svg)](https://www.codacy.com/app/orbisgis/h2gis?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=orbisgis/h2gis&amp;utm_campaign=Badge_Grade) 

H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database engine in the spirit of [PostGIS](http://postgis.net/). It adds support for managing spatial features and operations on the new `Geometry` type of H2, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and additional spatial functions that we (the [CNRS](http://www.cnrs.fr/))
develop. 

H2GIS is the root project for the [OrbisGIS](http://www.orbisgis.org/) data management library. It contains tools to run geometry analysis and read/write geospatial file formats.

H2GIS is licensed under the [LGPL 3](https://www.gnu.org/licenses/lgpl-3.0.fr.html) license terms.

## Official website & documentation

To find out more about this tool, including detailed documentation, please visit [www.h2gis.org](http://www.h2gis.org/).

## GEOMETRY data type

Since H2 2.2.X version, the [geometry](https://h2database.com/html/datatypes.html?highlight=geometry&search=geometry#geometry_type) encoding to store the value in H2 is the EWKB (extended well-known binary) format. The [EWKB](https://postgis.net/docs/using_postgis_dbmanagement.html#EWKB_EWKT) format is not an OGC standard, but a PostGIS specific format that includes the spatial reference system (SRID) identifier.
Its textual representation using the WKT (well-known text) uses the pattern :

```
'SRID=4326;POINT(0 0)'
```
H2 supports POINT, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON, and GEOMETRYCOLLECTION geometries with the following coordinate dimension 2D (XY), Z (XYZ), M (XYM), and ZM (XYZM).

H2 provides the same syntax as PostGIS to build a table with a geometry data type.

Examples:

```sql
CREATE TABLE mygeometrytable (ID INTEGER, GEOM GEOMETRY);
CREATE TABLE mygeometrytable (ID INTEGER, GEOM GEOMETRY(POINT));
CREATE TABLE mygeometrytable (ID INTEGER, GEOM GEOMETRY(POINT, 4326));
CREATE TABLE mygeometrytable (ID INTEGER, GEOM GEOMETRY(POINTZ, 4326));
CREATE TABLE mygeometrytable (ID INTEGER, GEOM GEOMETRY(POINTZM, 4326));
CREATE TABLE mygeometrytable (ID INTEGER, GEOM GEOMETRY(POINTM, 4326));
```

## Spatial functions

h2gis-functions is the main module of the H2GIS distribution. 
It extends H2 by adding analysis capabilities,including
* spatial operators (`ST_Intersection`, `ST_Difference`, etc.)
* spatial predicates (`ST_Intersects`, `ST_Contains`, etc.)
* additional spatial SQL functions that are not in [Simple Features for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL)

Ex: `ST_Extent`, `ST_Explode`, `ST_MakeGrid`

H2GIS contains a set of driver functions (I/O)) to read/write file formats such as .shp, .dbf, .geojson, .gpx

This I/O package include 2 implementation of TableEngine that allow you to immediatly 'link' a table with a shape file.

It include also file copy functions (import):
* SHPREAD( ) and SHPWRITE( ) to read and write Esri shape files.
* DBFREAD( ) and DBFWRITE( ) to read and write DBase III files.
* GeoJsonRead() and GeoJsonWrite() 
* to read and write GeoJSON files.
* GPXRead() to read GPX files.

## Usage

H2GIS requires Java 11. Run `maven clean install -P standalone` in the H2GIS's root directory.

In the folder `h2gis-dist/target/` you will find a zip file `h2gis-standalone-bin.zip`.Unzip the file then open `h2gis-dist-xxx.jar` It will open a browser based console application.

```bash
~ $ unzip h2gis-standalone-bin.zip

~ $ cd h2gis-standalone

~/h2gis-standalone $ java -jar h2gis-dist-xxx.jar
```
Click Connect in the web interface

[Create a database](http://www.h2database.com/html/quickstart.html) and run the following commands to add spatial features (do it only after the creation of a new database):

### Initialize the H2GIS extension

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

## Contributing

For legal reasons, contributors are asked to provide a contributor license agreement (CLA). 

The message need to include the following statement:

```
"I wrote the code, it's mine, and I'm contributing it to H2GIS for distribution licensed under the [LGPL 3.0](http://www.gnu.org/copyleft/lgpl.html)." 
```

If you want to contribute, send a Pull Request (PR) on GitHub and refer the CLA in your message.

## Download

To download the last H2GIS stable release and find documentation please go to [http://www.h2gis.org](http://www.h2gis.org)


## Include H2GIS into projects

You can include H2GIS in your project thanks to Maven repositories.

From maven central, check https://search.maven.org/artifact/org.orbisgis/h2gis/2.2.1/bundle

To use the current snapshot, just add the following lines in your pom

```xml
<repository>
  <id>orbisgis-snapshot</id>
  <name>OrbisGIS sonatype snapshot repository</name>
  <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</repository>
```

## Acknowledgements

The H2GIS team uses open source software. Specifically, we would like to thank  :

* Thomas Mueller, Noel Grandin and Evgenij Ryazanov from the [H2 database community](http://www.h2database.com).
* Martin Davis from the [JTS Topology Suite community](https://github.com/locationtech/jts).
* Michaël Michaud from [OpenJump community](https://github.com/openjump-gis)

## Supporters

Many thanks for those who reported bugs or provide patches...  


## Team

H2GIS is leaded by scientists and engineers in GIS and informatic sciences from [CNRS](https://www.cnrs.fr/) within the French [Lab-STICC](https://labsticc.fr/fr) laboratory ([DECIDE](https://labsticc.fr/fr/equipes/decide) team of Vannes).


## Fundings

H2GIS is funded by public research programs.