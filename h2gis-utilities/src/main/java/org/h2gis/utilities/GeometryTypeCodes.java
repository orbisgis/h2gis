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

package org.h2gis.utilities;

/**
 * Geometry type codes as defined in OGC SFS 1.2.1
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public interface GeometryTypeCodes {
    int Z = 1000;
    int M = 2000;

    //2D
    int GEOMETRY            = 0;
    int POINT               = 1;
    int LINESTRING          = 2;
    int POLYGON             = 3;
    int MULTIPOINT          = 4;
    int MULTILINESTRING     = 5;
    int MULTIPOLYGON        = 6;
    int GEOMCOLLECTION      = 7;
    //Reserved code
    //int CIRCULARSTRING      = 8;
    //Reserved code
    //int COMPOUNDCURVE       = 9;
    //Reserved code
    //int CURVEPOLYGON        = 10;
    int MULTICURVE          = 11;
    int MULTISURFACE        = 12;
    int CURVE               = 13;
    int SURFACE             = 14;
    int POLYHEDRALSURFACE   = 15;
    int TIN                 = 16;
    int TRIANGLE            = 17;

    //Z
    int GEOMETRYZ            = Z + GEOMETRY;
    int POINTZ               = Z + POINT;
    int LINESTRINGZ          = Z + LINESTRING;
    int POLYGONZ             = Z + POLYGON;
    int MULTIPOINTZ          = Z + MULTIPOINT;
    int MULTILINESTRINGZ     = Z + MULTILINESTRING;
    int MULTIPOLYGONZ        = Z + MULTIPOLYGON;
    int GEOMCOLLECTIONZ      = Z + GEOMCOLLECTION;
    //Reserved code
    //int CIRCULARSTRINGZ      = Z + CIRCULARSTRING;
    //Reserved code
    //int COMPOUNDCURVEZ       = Z + COMPOUNDCURVE;
    //Reserved code
    //int CURVEPOLYGONZ        = Z + CURVEPOLYGON;
    int MULTICURVEZ          = Z + MULTICURVE;
    int MULTISURFACEZ        = Z + MULTISURFACE;
    int CURVEZ               = Z + CURVE;
    int SURFACEZ             = Z + SURFACE;
    int POLYHEDRALSURFACEZ   = Z + POLYHEDRALSURFACE;
    int TINZ                 = Z + TIN;
    int TRIANGLEZ            = Z + TRIANGLE;

    //M
    int GEOMETRYM            = M + GEOMETRY;
    int POINTM               = M + POINT;
    int LINESTRINGM          = M + LINESTRING;
    int POLYGONM             = M + POLYGON;
    int MULTIPOINTM          = M + MULTIPOINT;
    int MULTILINESTRINGM     = M + MULTILINESTRING;
    int MULTIPOLYGONM        = M + MULTIPOLYGON;
    int GEOMCOLLECTIONM      = M + GEOMCOLLECTION;
    //Reserved code
    //int CIRCULARSTRINGM      = M + CIRCULARSTRING;
    //Reserved code
    //int COMPOUNDCURVEM       = M + COMPOUNDCURVE;
    //Reserved code
    //int CURVEPOLYGONM        = M + CURVEPOLYGON;
    int MULTICURVEM          = M + MULTICURVE;
    int MULTISURFACEM        = M + MULTISURFACE;
    int CURVEM               = M + CURVE;
    int SURFACEM             = M + SURFACE;
    int POLYHEDRALSURFACEM   = M + POLYHEDRALSURFACE;
    int TINM                 = M + TIN;
    int TRIANGLEM            = M + TRIANGLE;

    //ZM
    int GEOMETRYZM           = Z + M + GEOMETRY;
    int POINTZM              = Z + M + POINT;
    int LINESTRINGZM         = Z + M + LINESTRING;
    int POLYGONZM            = Z + M + POLYGON;
    int MULTIPOINTZM         = Z + M + MULTIPOINT;
    int MULTILINESTRINGZM    = Z + M + MULTILINESTRING;
    int MULTIPOLYGONZM       = Z + M + MULTIPOLYGON;
    int GEOMCOLLECTIONZM     = Z + M + GEOMCOLLECTION;
    //Reserved code
    //int CIRCULARSTRINGZM     = Z + M + CIRCULARSTRING;
    //Reserved code
    //int COMPOUNDCURVEZM      = Z + M + COMPOUNDCURVE;
    //Reserved code
    //int CURVEPOLYGONZM       = Z + M + CURVEPOLYGON;
    int MULTICURVEZM         = Z + M + MULTICURVE;
    int MULTISURFACEZM       = Z + M + MULTISURFACE;
    int CURVEZM              = Z + M + CURVE;
    int SURFACEZM            = Z + M + SURFACE;
    int POLYHEDRALSURFACEZM  = Z + M + POLYHEDRALSURFACE;
    int TINZM                = Z + M + TIN;
    int TRIANGLEZM           = Z + M + TRIANGLE;
}
