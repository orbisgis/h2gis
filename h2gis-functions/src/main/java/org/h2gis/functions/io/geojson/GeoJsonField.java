/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.geojson;

/**
 * GeoJson fields used by the standard.
 * @author Erwan Bocher
 * @author Hai Trung Pham
 */
public class GeoJsonField {

    public static String NAME="name";
    public static String CRS ="crs"; // 2008
    public static String FEATURES="features";
    public static String FEATURECOLLECTION="featurecollection";
    public static String FEATURE="feature";
    public static String GEOMETRY="geometry";
    public static String PROPERTIES="properties";

    /**
     * If a Feature has a commonly used identifier, that identifier
     *       SHOULD be included as a member of the Feature object with the name
     *       "id", and the value of this member is either a JSON string or
     *       number.
     */
    static public String FEATURE_ID="id";
    static public String POINT="point";
    static public String LINESTRING="linestring";
    static public String POLYGON="polygon";
    static public String MULTIPOINT="multipoint";
    static public String MULTILINESTRING="multilinestring";
    static public String MULTIPOLYGON="multipolygon";
    static public String COORDINATES="coordinates";
    static public String GEOMETRYCOLLECTION="geometrycollection";
    static public String GEOMETRIES="geometries";
    static public String CRS_URN_EPSG="urn:ogc:def:crs:epsg::"; // 2008
    static public String CRS_URN_OGC="urn:ogc:def:crs:ogc:1.3:CRS84"; // 2008
    static public String LINK="link"; // 2008
    static public String BBOX="bbox";
    static public  String TYPE="type";

}
