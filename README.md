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

- a new `Geometry` data type storing `POINT`, `CURVE` and `SURFACE` types in
  WKB representations
- spatial operators (`ST_Intersection`, `ST_Difference`, etc.)
- spatial predicates (`ST_Intersects`, `ST_Contains`, etc.)
- R-Tree indexing

### H2Drivers
H2Drivers makes use of the TableEngine API to add H2 read/write support for file
formats such as .shp and .csv. With H2Drivers, the user can execute SQL queries
on file formats other than pure H2.

Both H2Spatial and H2Drivers are licensed under the GPL 3 license terms.
           
# How to Use
Follow [H2](http://www.h2database.com/) tutorial to download and install H2. Add h2spatial jar and [JTS](http://sourceforge.net/projects/jts-topo-suite/) 1.12 jar in the application Class Path.

[Create a DataBase](http://www.h2database.com/html/quickstart.html) and run the following commands to add spatial features:

```sql
CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR "CreateSpatialExtension.initSpatialExtension";
CALL SPATIAL_INIT();
```
