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
* H2Spatial
H2Spatial is a set of functions that permit to extend H2 with spatial storage and analysis capabilities. 
It includes  :

- a new data type called geometry that stored POINT, CURVE, SURFACE in a WKB representation,
- spatial operators as ST_Intersection, St_Difference...,
- spatial predicates as ST_Intersects, ST_Contains,

H2Spatial is licensed under the GPL 3 license terms.


* H2Drivers

H2Drivers is licensed under the GPL 3 license terms.
