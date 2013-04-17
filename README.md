H2GIS
=====

H2GIS is a spatial extension of the [H2](http://www.h2database.com/) database
engine in the spirit of [PostGIS](http://postgis.net/). It adds support for
managing spatial features and operations including a `Geometry` type, the [Open
Geospatial Consortium](http://www.opengeospatial.org/) (OGC) [Simple Features
for SQL](http://www.opengeospatial.org/standards/sfs) (SFSQL) functions and
additional spatial functions that we (the [Atelier SIG](http://www.irstv.fr/))
develop. 

H2GIS is the main project for the new [OrbisGIS](http://www.orbisgis.org/) data
management library and is divided into 2 subprojects:

1. H2Spatial
1. H2Drivers

H2Spatial extends H2 by adding spatial storage and analysis capabilities, including

- a new `Geometry` data type storing `POINT`, `CURVE` and `SURFACE` types in WKB representations
- spatial operators (`ST_Intersection`, `ST_Difference`, etc.)
- spatial predicates (`ST_Intersects`, `ST_Contains`, etc.)
- R-Tree indexing

* H2Drivers

H2Drivers is a collection of drivers to read and write new formats with H2. 
H2Drivers takes advantage of the TableEngine API to wrap in the H2 table model file format as shapefile, csv, ascii...
With the H2Drivers a user can execute SQL queries on another disk file format than H2.


Both H2Spatial and H2Drivers are licensed under the GPL 3 license terms.
