/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.geojson;

/**
 * GeoJson fields used by the standard.
 *
 * Some of them will only be used by GeoJSON
 * files of the 2008 specification.
 * ("CRS", "CRS_URN_EPSG", "CRS_URN_OGC": These
 * fields are no longer useful for the new
 * standard RFC 7946 because it uses WGS 84
 * by default.
 * "LINK": link object doesn't exist in RFC 7946.)
 *
 * Although, these fields will be kept for
 * compatibility reasons and indicated "2008" in
 * comment.
 *
 * New field "BBOX" is added for interoperability
 * reasons.
 * 
 * @author Erwan Bocher
 */
public class GeoJsonField {
    
    static String NAME="name";
    static String CRS ="crs"; // 2008
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
    static String CRS_URN_EPSG="urn:ogc:def:crs:epsg::"; // 2008
    static String CRS_URN_OGC="urn:ogc:def:crs:ogc:1.3:"; // 2008
    static String LINK="link"; // 2008
    static String BBOX="bbox";
    
}
