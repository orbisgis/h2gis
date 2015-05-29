/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.drivers.geojson;

/**
 * GeoJson fields used by the standard.
 * 
 * @author Erwan Bocher
 */
public class GeoJsonField {
    
    static String NAME="name";
    static String CRS ="crs";
    static String FEATURES="features";
    static String FEATURECOLLECTION="featurecollection";
    static String FEATURE="feature";
    static String GEOMETRY="geometry";
    static String PROPERTIES="properties";
    static String POINT="point";
    static String LINESTRING="linestring";
    static String POLYGON="polygon";
    static String MULTIPOINT="multipoint";
    static String MULTILINESTRING="multilinestring";
    static String MULTIPOLYGON="multipolygon";
    static String COORDINATES="coordinates";
    static String GEOMETRYCOLLECTION="geometrycollection";
    static String GEOMETRIES="geometries";
    static String CRS_URN_EPSG="urn:ogc:def:crs:epsg::";
    static String CRS_URN_OGC="urn:ogc:def:crs:ogc:1.3:";
    static String LINK="link";
        
    
}
