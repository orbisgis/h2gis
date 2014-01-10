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
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Tools to convert JTS geometry to KML representation
 *
 * @author Erwan Bocher
 */
public class KMLGeometry {

    private KMLGeometry() {
    }

    /**
     * A geographic location defined by longitude, latitude, and (optional)
     * altitude
     *
     * Supported syntax :
     * <Point>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </Point>
     *
     * @param point
     * @return
     */
    public static void toKMLPoint(Point point, StringBuilder sb ) {
        sb.append("<Point><coordinates>");
        Coordinate coord = point.getCoordinate();
        sb.append(coord.y).append(",").append(coord.x);
        if (!Double.isNaN(coord.z)) {
            sb.append(",").append(coord.z);
        }
        sb.append("</coordinates>").append("</Point>");
    }

    /**
     * 
     * @param lineString
     * @return 
     */
    public static void toKMLLineString(LineString lineString,  StringBuilder sb) {
        sb.append("<LineString>");
        appendKMLCoordinates(lineString.getCoordinates(), sb);
        sb.append("</LineString>");       
    }
    
    /**
     * Transform a linestring to a kml linearRing
     * 
     * 
     * @param lineString
     */
    public static void toKMLLinearRing(LineString lineString,  StringBuilder sb) {
        sb.append("<LinearRing>");
        appendKMLCoordinates(lineString.getCoordinates(), sb);
        sb.append("</LinearRing>");
    }
    
    /**
     * 
     * @param polygon
     * @return 
     */
    public static void toKMLPolygon(Polygon polygon, StringBuilder sb) {
        sb.append("<Polygon>");        
        sb.append("<outerBoundaryIs>");
        toKMLLinearRing(polygon.getExteriorRing(), sb);
        appendKMLCoordinates(polygon.getExteriorRing().getCoordinates(), sb);
        sb.append("</LinearRing>");        
        sb.append("</outerBoundaryIs>");
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            sb.append("<innerBoundaryIs>");
            toKMLLinearRing( polygon.getInteriorRingN(i), sb);
            sb.append("</innerBoundaryIs>");
        } 
    }
    
    /**
     * 
     * @param gc
     */
    public static void toKMLMultiGeometry(GeometryCollection gc, StringBuilder sb) {
       sb.append("<MultiGeometry>");
        for (int i = 0; i < gc.getNumGeometries(); i++) {
            Geometry geom = gc.getGeometryN(i);
            if (geom instanceof Point) {
                toKMLPoint((Point) geom, sb);
            } else if (geom instanceof LineString) {
                toKMLLineString((LineString) geom, sb);
            } else if (geom instanceof Polygon) {
                toKMLPolygon( (Polygon) geom, sb);
            }
        }
        sb.append("</MultiGeometry>");
    }

    /** Build a string represention to kml coordinates
     * 
     *  <coordinates>...</coordinates> <!-- lon,lat[,alt] tuples -->
     * @param coords
     * @return 
     */
    public static void appendKMLCoordinates(Coordinate[] coords, StringBuilder sb) {
        sb.append("<coordinates>");
        for (Coordinate coord : coords) {
            sb.append(coord.y).append(",").append(coord.x);
            if (!Double.isNaN(coord.z)) {
                sb.append(",").append(coord.z);
            }
            sb.append(" ");
        }
        sb.append("</coordinates>");
    }
}
