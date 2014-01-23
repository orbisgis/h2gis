/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.drivers.kml;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.sql.SQLException;

/**
 * Tools to convert JTS geometry to KML representation
 *
 * @author Erwan Bocher
 */
public class KMLGeometry {

    

    private KMLGeometry() {
    }

    /**
     * Convert JTS geometry to a kml geomtry representation.
     *
     * @param geometry
     */
    public static void toKMLGeometry(Geometry geometry, boolean extrude, AltitudeModeEnum altitudeModeEnum, StringBuilder sb) throws SQLException {
        if (geometry instanceof Point) {
            toKMLPoint((Point) geometry, extrude, altitudeModeEnum, sb);
        } else if (geometry instanceof LineString) {
            toKMLLineString((LineString) geometry,extrude,altitudeModeEnum,  sb);
        } else if (geometry instanceof Polygon) {
            toKMLPolygon((Polygon) geometry,extrude,altitudeModeEnum, sb);
        } else if (geometry instanceof GeometryCollection) {
            toKMLMultiGeometry((GeometryCollection) geometry,extrude, altitudeModeEnum, sb);
        } else {
            throw new SQLException("This geometry type is not supported : " + geometry.toString());
        }
    }

    /**
     * A geographic location defined by longitude, latitude, and (optional)
     * altitude.
     *
     * Syntax :
     *
     * <Point id="ID">
     * <!-- specific to Point -->
     * <extrude>0</extrude> <!-- boolean -->
     * <altitudeMode>clampToGround</altitudeMode>
     * <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute
     * -->
     * <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor
     * -->
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </Point>
     *
     * Supported syntax :
     * <Point>
     * <extrude>0</extrude>
     * <altitudeMode>clampToGround</altitudeMode>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </Point>
     *
     * @param point
     */
    public static void toKMLPoint(Point point, boolean extrude, AltitudeModeEnum altitudeModeEnum, StringBuilder sb) {
        sb.append("<Point>");
        appendExtrude(extrude, sb);
        sb.append("<altitudeMode>").append(altitudeModeEnum).append("</altitudeMode>");
        sb.append("<coordinates>");
        Coordinate coord = point.getCoordinate();
        sb.append(coord.x).append(",").append(coord.y);
        if (!Double.isNaN(coord.z)) {
            sb.append(",").append(coord.z);
        }
        sb.append("</coordinates>").append("</Point>");
    }

    /**
     * Defines a connected set of line segments.
     *
     * Syntax :
     *
     * <LineString id="ID">
     * <!-- specific to LineString -->
     * <gx:altitudeOffset>0</gx:altitudeOffset> <!-- double -->
     * <extrude>0</extrude> <!-- boolean -->
     * <tessellate>0</tessellate> <!-- boolean -->
     * <altitudeMode>clampToGround</altitudeMode>
     * <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute
     * -->
     * <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor
     * -->
     * <gx:drawOrder>0</gx:drawOrder> <!-- integer -->
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LineString>
     *
     * Supported syntax :
     *
     * <LineString>
     * <extrude>0</extrude>
     * <altitudeMode>clampToGround</altitudeMode>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LineString>
     *
     * @param lineString
     */
    public static void toKMLLineString(LineString lineString, boolean  extrude,AltitudeModeEnum altitudeModeEnum, StringBuilder sb) {
        sb.append("<LineString>");
        appendExtrude(extrude, sb);
        sb.append("<altitudeMode>").append(altitudeModeEnum).append("</altitudeMode>");
        sb.append("<coordinates>");
        appendKMLCoordinates(lineString.getCoordinates(), sb);
        sb.append("</LineString>");
    }

    /**
     * Defines a closed line string, typically the outer boundary of a Polygon.
     *
     * Syntax :
     *
     * <LinearRing id="ID">
     * <!-- specific to LinearRing -->
     * <gx:altitudeOffset>0</gx:altitudeOffset> <!-- double -->
     * <extrude>0</extrude> <!-- boolean -->
     * <tessellate>0</tessellate> <!-- boolean -->
     * <altitudeMode>clampToGround</altitudeMode>
     * <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute
     * -->
     * <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor
     * -->
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] tuples -->
     * </LinearRing>
     *
     * Supported syntax :
     *
     * <LinearRing>
     * <extrude>0</extrude>
     * <altitudeMode>clampToGround</altitudeMode>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LinearRing>
     *
     * @param lineString
     */
    public static void toKMLLinearRing(LineString lineString, boolean extrude, AltitudeModeEnum altitudeModeEnum, StringBuilder sb) {
        sb.append("<LinearRing>");
        appendExtrude(extrude, sb);
        sb.append("<altitudeMode>").append(altitudeModeEnum).append("</altitudeMode>");
        appendKMLCoordinates(lineString.getCoordinates(), sb);
        sb.append("</LinearRing>");
    }

    /**
     * A Polygon is defined by an outer boundary and 0 or more inner boundaries.
     * The boundaries, in turn, are defined by LinearRings.
     *
     * Syntax :
     *
     * <Polygon id="ID">
     * <!-- specific to Polygon -->
     * <extrude>0</extrude> <!-- boolean -->
     * <tessellate>0</tessellate> <!-- boolean -->
     * <altitudeMode>clampToGround</altitudeMode>
     * <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute
     * -->
     * <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor
     * -->
     * <outerBoundaryIs>
     * <LinearRing>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LinearRing>
     * </outerBoundaryIs>
     * <innerBoundaryIs>
     * <LinearRing>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LinearRing>
     * </innerBoundaryIs>
     * </Polygon>
     *
     * Supported syntax :
     *
     * <Polygon>
     * <extrude>0</extrude>
     * <altitudeMode>clampToGround</altitudeMode>
     * <outerBoundaryIs>
     * <LinearRing>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LinearRing>
     * </outerBoundaryIs>
     * <innerBoundaryIs>
     * <LinearRing>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LinearRing>
     * </innerBoundaryIs>
     * </Polygon>
     *
     * @param polygon
     */
    public static void toKMLPolygon(Polygon polygon,boolean extrude,AltitudeModeEnum altitudeModeEnum, StringBuilder sb) {
        sb.append("<Polygon>");
        appendExtrude(extrude, sb);
        sb.append("<altitudeMode>").append(altitudeModeEnum).append("</altitudeMode>");
        sb.append("<outerBoundaryIs>");
        toKMLLinearRing(polygon.getExteriorRing(),extrude, altitudeModeEnum, sb);
        sb.append("</outerBoundaryIs>");
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            sb.append("<innerBoundaryIs>");
            toKMLLinearRing(polygon.getInteriorRingN(i), extrude,altitudeModeEnum, sb);
            sb.append("</innerBoundaryIs>");
        }
        sb.append("</Polygon>");
    }

    /**
     *
     *
     * A container for zero or more geometry primitives associated with the same
     * feature.
     *
     * <MultiGeometry id="ID">
     * <!-- specific to MultiGeometry -->
     * <!-- 0 or more Geometry elements -->
     * </MultiGeometry>
     *
     * @param gc
     */
    public static void toKMLMultiGeometry(GeometryCollection gc, boolean extrude, AltitudeModeEnum altitudeModeEnum, StringBuilder sb) {
        sb.append("<MultiGeometry>");
        for (int i = 0; i < gc.getNumGeometries(); i++) {
            Geometry geom = gc.getGeometryN(i);
            if (geom instanceof Point) {
                toKMLPoint((Point) geom,extrude, altitudeModeEnum, sb);
            } else if (geom instanceof LineString) {
                toKMLLineString((LineString) geom,extrude, altitudeModeEnum, sb);
            } else if (geom instanceof Polygon) {
                toKMLPolygon((Polygon) geom, extrude,altitudeModeEnum, sb);
            }
        }
        sb.append("</MultiGeometry>");
    }

    /**
     * Build a string represention to kml coordinates
     *
     * Syntax :
     *
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] tuples -->
     *
     * @param coords
     */
    public static void appendKMLCoordinates(Coordinate[] coords, StringBuilder sb) {
        sb.append("<coordinates>");
        for (int i = 0; i < coords.length; i++) {
            Coordinate coord = coords[i];
            sb.append(coord.x).append(",").append(coord.y);
            if (!Double.isNaN(coord.z)) {
                sb.append(",").append(coord.z);
            }
            if (i < coords.length - 1) {
                sb.append(" ");
            }
        }
        sb.append("</coordinates>");
    }
    
    /**
     * Append the extrude value
     * 
     * Syntax :
     * 
     * <extrude>0</extrude>
     * 
     * @param extrude
     * @param sb 
     */
    private static void appendExtrude(boolean extrude, StringBuilder sb) {
       if(extrude){
           sb.append("<extrude>").append(1).append("</extrude>");
       }else{
           sb.append("<extrude>").append(0).append("</extrude>");
       }
    }
}
