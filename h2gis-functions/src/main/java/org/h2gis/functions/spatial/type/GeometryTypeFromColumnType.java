/*
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

package org.h2gis.functions.spatial.type;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryTypeCodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert H2 column type string into a OGC geometry type index.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class GeometryTypeFromColumnType extends DeterministicScalarFunction {
    //private static final Pattern PATTERN = Pattern.compile("\"(.*)\"|GEOMETRY\\((.*)\\)");
    private static final Pattern PATTERN = Pattern.compile("^GEOMETRY\\s*\\(\\s*([^),]+)\\s*[\\),]|(^[^ ]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_CHECK = Pattern.compile(
            "\"?ST_GEOMETRYTYPECODE\\s*\"?\\(([^)]+)\\)\\s*([<|>|!]?=|<>|>|<)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Default constructor
     */
    public GeometryTypeFromColumnType() {
        addProperty(PROP_REMARKS, "Convert H2 column type string into a OGC geometry type index.");
        addProperty(PROP_NAME, "_GeometryTypeFromColumnType");
    }

    @Override
    public String getJavaStaticMethod() {
        return "geometryTypeFromColumnType";
    }

    /**
     * Convert H2 column type string into a OGC geometry type index.
     *
     * @param columnType H2 column type
     *
     * @return Geometry type code {@link GeometryTypeCodes}
     */
    public static int geometryTypeFromColumnType(String columnType) {
        String type = null;
        Matcher matcher = PATTERN.matcher(columnType);
        while (matcher.find()) {
            if(type == null) {
                type = matcher.group(1);
            }
            if(type == null) {
                type = matcher.group(2);
                if(type.equals("GEOMETRY")){
                    Matcher checkMatcher = PATTERN_CHECK.matcher(columnType);
                    if (checkMatcher.find())
                        if (checkMatcher.group(2) != null) {
                            return Integer.parseInt(checkMatcher.group(3));
                        }
                }
            }
        }
        return typeToCode(type);
    }


    private static int typeToCode(String type){
        if(type == null){
            return GeometryTypeCodes.GEOMETRY;
        }
        type = type.replaceAll(" ", "").replaceAll("\"", "");
        switch(type){
            case "POINT" :              return GeometryTypeCodes.POINT;
            case "LINESTRING" :         return GeometryTypeCodes.LINESTRING;
            case "POLYGON" :            return GeometryTypeCodes.POLYGON;
            case "MULTIPOINT" :         return GeometryTypeCodes.MULTIPOINT;
            case "MULTILINESTRING" :    return GeometryTypeCodes.MULTILINESTRING;
            case "MULTIPOLYGON" :       return GeometryTypeCodes.MULTIPOLYGON;
            case "GEOMETRYCOLLECTION" :     return GeometryTypeCodes.GEOMCOLLECTION;
            case "MULTICURVE" :         return GeometryTypeCodes.MULTICURVE;
            case "MULTISURFACE" :       return GeometryTypeCodes.MULTISURFACE;
            case "CURVE" :              return GeometryTypeCodes.CURVE;
            case "SURFACE" :            return GeometryTypeCodes.SURFACE;
            case "POLYHEDRALSURFACE" :  return GeometryTypeCodes.POLYHEDRALSURFACE;
            case "TIN" :                return GeometryTypeCodes.TIN;
            case "TRIANGLE" :           return GeometryTypeCodes.TRIANGLE;

            case "POINTZ" :              return GeometryTypeCodes.POINTZ;
            case "LINESTRINGZ" :         return GeometryTypeCodes.LINESTRINGZ;
            case "POLYGONZ" :            return GeometryTypeCodes.POLYGONZ;
            case "MULTIPOINTZ" :         return GeometryTypeCodes.MULTIPOINTZ;
            case "MULTILINESTRINGZ" :    return GeometryTypeCodes.MULTILINESTRINGZ;
            case "MULTIPOLYGONZ" :       return GeometryTypeCodes.MULTIPOLYGONZ;
            case "GEOMETRYCOLLECTIONZ" :     return GeometryTypeCodes.GEOMCOLLECTIONZ;
            case "MULTICURVEZ" :         return GeometryTypeCodes.MULTICURVEZ;
            case "MULTISURFACEZ" :       return GeometryTypeCodes.MULTISURFACEZ;
            case "CURVEZ" :              return GeometryTypeCodes.CURVEZ;
            case "SURFACEZ" :            return GeometryTypeCodes.SURFACEZ;
            case "POLYHEDRALSURFACEZ" :  return GeometryTypeCodes.POLYHEDRALSURFACEZ;
            case "TINZ" :                return GeometryTypeCodes.TINZ;
            case "TRIANGLEZ" :           return GeometryTypeCodes.TRIANGLEZ;

            case "POINTM" :             return GeometryTypeCodes.POINTM;
            case "LINESTRINGM" :        return GeometryTypeCodes.LINESTRINGM;
            case "POLYGONM" :           return GeometryTypeCodes.POLYGONM;
            case "MULTIPOINTM" :        return GeometryTypeCodes.MULTIPOINTM;
            case "MULTILINESTRINGM" :   return GeometryTypeCodes.MULTILINESTRINGM;
            case "MULTIPOLYGONM" :      return GeometryTypeCodes.MULTIPOLYGONM;
            case "GEOMETRYCOLLECTIONM" :    return GeometryTypeCodes.GEOMCOLLECTIONM;
            case "MULTICURVEM" :        return GeometryTypeCodes.MULTICURVEM;
            case "MULTISURFACEM" :      return GeometryTypeCodes.MULTISURFACEM;
            case "CURVEM" :             return GeometryTypeCodes.CURVEM;
            case "SURFACEM" :           return GeometryTypeCodes.SURFACEM;
            case "POLYHEDRALSURFACEM" : return GeometryTypeCodes.POLYHEDRALSURFACEM;
            case "TINM" :               return GeometryTypeCodes.TINM;
            case "TRIANGLEM" :          return GeometryTypeCodes.TRIANGLEM;

            case "POINTZM" :              return GeometryTypeCodes.POINTZM;
            case "LINESTRINGZM" :         return GeometryTypeCodes.LINESTRINGZM;
            case "POLYGONZM" :            return GeometryTypeCodes.POLYGONZM;
            case "MULTIPOINTZM" :         return GeometryTypeCodes.MULTIPOINTZM;
            case "MULTILINESTRINGZM" :    return GeometryTypeCodes.MULTILINESTRINGZM;
            case "MULTIPOLYGONZM" :       return GeometryTypeCodes.MULTIPOLYGONZM;
            case "GEOMETRYCOLLECTIONZM" :     return GeometryTypeCodes.GEOMCOLLECTIONZM;
            case "MULTICURVEZM" :         return GeometryTypeCodes.MULTICURVEZM;
            case "MULTISURFACEZM" :       return GeometryTypeCodes.MULTISURFACEZM;
            case "CURVEZM" :              return GeometryTypeCodes.CURVEZM;
            case "SURFACEZM" :            return GeometryTypeCodes.SURFACEZM;
            case "POLYHEDRALSURFACEZM" :  return GeometryTypeCodes.POLYHEDRALSURFACEZM;
            case "TINZM" :                return GeometryTypeCodes.TINZM;
            case "TRIANGLEZM" :           return GeometryTypeCodes.TRIANGLEZM;


            case "GEOMETRY" :
            default :                   return GeometryTypeCodes.GEOMETRY;
        }
    }
}
