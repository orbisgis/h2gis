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

package org.h2gis.functions.io.kml;

import org.locationtech.jts.geom.*;

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
     * Convert JTS geometry to a kml geometry representation.
     * 
     * @param geom input geometry
     * @param sb buffer to store the KML
     */
    public static void toKMLGeometry(Geometry geom, StringBuilder sb) throws SQLException {
        toKMLGeometry(geom, ExtrudeMode.NONE, AltitudeMode.NONE, sb);
    }

    /**
     * Convert JTS geometry to a kml geometry representation.
     *
     * @param geometry input geometry
     * @param extrude extrude mode
     * @param altitudeModeEnum altitude mode
     * @param sb buffer to store the KML
     */
    public static void toKMLGeometry(Geometry geometry, ExtrudeMode extrude, int altitudeModeEnum, StringBuilder sb) throws SQLException {
        if (geometry instanceof Point) {
            toKMLPoint((Point) geometry, extrude, altitudeModeEnum, sb);
        } else if (geometry instanceof LineString) {
            toKMLLineString((LineString) geometry, extrude, altitudeModeEnum, sb);
        } else if (geometry instanceof Polygon) {
            toKMLPolygon((Polygon) geometry, extrude, altitudeModeEnum, sb);
        } else if (geometry instanceof GeometryCollection) {
            toKMLMultiGeometry((GeometryCollection) geometry, extrude, altitudeModeEnum, sb);
        } else {
            throw new SQLException("This geometry type is not supported : " + geometry.toString());
        }
    }

    /**
     * A geographic location defined by longitude, latitude, and (optional)
     * altitude.
     *
     * Syntax :
     * {@code
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
     * }
     * Supported syntax :
     * {@code
     * <Point>
     * <extrude>0</extrude>
     * <altitudeMode>clampToGround</altitudeMode>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </Point>
     * }
     * @param point point
     * @param extrude extrude mode
     * @param altitudeModeEnum altitude mode
     */
    public static void toKMLPoint(Point point, ExtrudeMode extrude, int altitudeModeEnum, StringBuilder sb) {
        sb.append("<Point>");
        appendExtrude(extrude, sb);
        appendAltitudeMode(altitudeModeEnum, sb);
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
     * {@code
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
     * }
     * Supported syntax :
     * {@code
     * <LineString>
     * <extrude>0</extrude>
     * <altitudeMode>clampToGround</altitudeMode>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LineString>
     * }
     */
    public static void toKMLLineString(LineString lineString, ExtrudeMode extrude, int altitudeModeEnum, StringBuilder sb) {
        sb.append("<LineString>");
        appendExtrude(extrude, sb);
        appendAltitudeMode(altitudeModeEnum, sb);
        appendKMLCoordinates(lineString.getCoordinates(), sb);
        sb.append("</LineString>");
    }

    /**
     * Defines a closed line string, typically the outer boundary of a Polygon.
     *
     * Syntax :
     * {@code
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
     * }
     * Supported syntax :
     * {@code
     * <LinearRing>
     * <extrude>0</extrude>
     * <altitudeMode>clampToGround</altitudeMode>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LinearRing>
     * }
     *
     */
    public static void toKMLLinearRing(LineString lineString, ExtrudeMode extrude, int altitudeModeEnum, StringBuilder sb) {
        sb.append("<LinearRing>");
        appendExtrude(extrude, sb);
        appendAltitudeMode(altitudeModeEnum, sb);
        appendKMLCoordinates(lineString.getCoordinates(), sb);
        sb.append("</LinearRing>");
    }

    /**
     * A Polygon is defined by an outer boundary and 0 or more inner boundaries.
     * The boundaries, in turn, are defined by LinearRings.
     *
     * Syntax :
     * {@code
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
     * }
     * Supported syntax :
     * {@code
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
     * }
     *
     */
    public static void toKMLPolygon(Polygon polygon, ExtrudeMode extrude, int altitudeModeEnum, StringBuilder sb) {
        sb.append("<Polygon>");
        appendExtrude(extrude, sb);
        appendAltitudeMode(altitudeModeEnum, sb);
        sb.append("<outerBoundaryIs>");
        toKMLLinearRing(polygon.getExteriorRing(), extrude, altitudeModeEnum, sb);
        sb.append("</outerBoundaryIs>");
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            sb.append("<innerBoundaryIs>");
            toKMLLinearRing(polygon.getInteriorRingN(i), extrude, altitudeModeEnum, sb);
            sb.append("</innerBoundaryIs>");
        }
        sb.append("</Polygon>");
    }

    /**
     *
     *
     * A container for zero or more geometry primitives associated with the same
     * feature.
     *  {@code
     * <MultiGeometry id="ID">
     * <!-- specific to MultiGeometry -->
     * <!-- 0 or more Geometry elements -->
     * </MultiGeometry>
     * }
     *
     */
    public static void toKMLMultiGeometry(GeometryCollection gc, ExtrudeMode extrude, int altitudeModeEnum, StringBuilder sb) {
        sb.append("<MultiGeometry>");
        int size = gc.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry geom = gc.getGeometryN(i);
            if (geom instanceof Point) {
                toKMLPoint((Point) geom, extrude, altitudeModeEnum, sb);
            } else if (geom instanceof LineString) {
                toKMLLineString((LineString) geom, extrude, altitudeModeEnum, sb);
            } else if (geom instanceof Polygon) {
                toKMLPolygon((Polygon) geom, extrude, altitudeModeEnum, sb);
            }
        }
        sb.append("</MultiGeometry>");
    }

    /**
     * Build a string represention to kml coordinates
     *
     * Syntax :
     * {@code
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] tuples -->
     * }
     * @param coords coordinates array
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
     * @param extrude {@link ExtrudeMode}
     * @param sb buffer to store the KML
     */
    private static void appendExtrude(ExtrudeMode extrude, StringBuilder sb) {
        if (extrude.equals(ExtrudeMode.TRUE)) {
            sb.append("<extrude>").append(1).append("</extrude>");
        } else if (extrude.equals(ExtrudeMode.FALSE)) {
            sb.append("<extrude>").append(0).append("</extrude>");
        }
    }

    /**
     * Append the altitudeMode
     *
     * Syntax :
     *
     * <altitudeMode>clampToGround</altitudeMode>
     *
     * @param altitudeModeEnum {@link AltitudeMode}
     * @param sb buffer to store the KML
     */
    private static void appendAltitudeMode(int altitudeModeEnum, StringBuilder sb) {
        AltitudeMode.append(altitudeModeEnum, sb);
    }
}
