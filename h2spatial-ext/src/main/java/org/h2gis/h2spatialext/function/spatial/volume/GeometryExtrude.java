/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.volume;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used to extrude a polygon or a linestring to a set of walls,
 * roof, or floor using a height.
 *
 * @author Erwan Bocher
 * @author Thomas Leduc
 */
public class GeometryExtrude {

    private static final GeometryFactory GF = new GeometryFactory();    

   

    private GeometryExtrude() {
    }
    
    /**
     * Extrude the polygon as a collection of geometries
     * The output geometryCollection contains the floor, the walls and the roof.
     * @param polygon
     * @param hight
     * @return 
     */
    public static GeometryCollection extrudePolygonAsGeometry(Polygon polygon, double hight){
        Geometry[] geometries = new Geometry[3];
        geometries[0]= extractFloor(polygon, hight);
        geometries[1]= extractWalls(polygon, hight);
        geometries[2]= extractRoof(polygon, hight);
        return GF.createGeometryCollection(geometries);
    }
    
     /**
     * Extrude the linestring as a collection of geometries
     * The output geometryCollection contains the floor, the walls and the roof.
     * @param linestring
     * @param hight
     * @return 
     */
    public static GeometryCollection extrudeLineStringAsGeometry(LineString lineString, double hight){
        Geometry[] geometries = new Geometry[3];
        geometries[0]= lineString;
        geometries[1]= extractWalls(lineString, hight);
        geometries[2]= extractRoof(lineString, hight);
        return GF.createGeometryCollection(geometries);
    }
    
    /**
     * Extract the linestring "roof". 
     * @param lineString
     * @param hight
     * @return 
     */
    public static Geometry extractRoof(LineString lineString, double hight) {
       return  GF.createLineString(translate(lineString, hight));
    }
    
    /**
     * Reverse the polygon to be oriented counter-clockwise
     * @param polygon
     * @param hightt
     * @return 
     */
    public static Polygon extractFloor(Polygon polygon, double hightt){
        return getClockWise(polygon);
    }
    
    /**
     * Extract the walls from a polygon
     * @param polygon
     * @param hight
     * @return 
     */
    public static MultiPolygon extractWalls(Polygon polygon, double hight){
        //We process the exterior ring 
        final LineString shell = getClockWise(polygon.getExteriorRing());

        ArrayList<Polygon> walls = new ArrayList<Polygon>();
        for (int i = 1; i < shell.getNumPoints(); i++) {
            walls.add(extrudeEdge(shell.getCoordinateN(i - 1), shell.getCoordinateN(i), hight));
        }

        // We create the walls  for all holes 
        int nbOfHoles = polygon.getNumInteriorRing();
        for (int i = 0; i < nbOfHoles; i++) {
            final LineString hole = getCounterClockWise(polygon.getInteriorRingN(i));
            for (int j = 1; j < hole.getNumPoints(); j++) {
                walls.add(extrudeEdge(hole.getCoordinateN(j - 1),
                        hole.getCoordinateN(j), hight));
            }
        }
        return GF.createMultiPolygon(walls.toArray(new Polygon[walls.size()]));
    }
    
    /**
     * Extract the roof of a polygon
     * 
     * @param polygon
     * @param hight
     * @return 
     */
    public static Polygon extractRoof(Polygon polygon, double hight){               
        final LinearRing upperShell = GF.createLinearRing(translate(polygon.getExteriorRing(), hight));
        int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = GF.createLinearRing(translate(polygon.getInteriorRingN(i), hight));
        }
        return getCounterClockWise(GF.createPolygon(upperShell, holes));
    }

    /**
     * Extrude the LineString as a set of walls.
     * @param lineString
     * @return
     */
    public static MultiPolygon extractWalls(LineString lineString, double hight) {
        //Extract the walls        
        Coordinate[] coords = lineString.getCoordinates();
        Polygon[] walls = new Polygon[coords.length - 1];
        for (int i = 0; i < coords.length - 1; i++) {
            walls[i] = extrudeEdge(coords[i], coords[i + 1], hight);
        }
        return GF.createMultiPolygon(walls);
    }

    /**
     * Reverse the LineString to be oriented clockwise.
     * @param lineString
     * @return 
     */
    private static LineString getClockWise(final LineString lineString) {
        final Coordinate c0 = lineString.getCoordinateN(0);
        final Coordinate c1 = lineString.getCoordinateN(1);
        final Coordinate c2 = lineString.getCoordinateN(2);

        if (CGAlgorithms.computeOrientation(c0, c1, c2) == CGAlgorithms.CLOCKWISE) {
            return lineString;
        } else {
            return (LineString) lineString.reverse();
        }
    }

    /**
     * Reverse the LineString to be oriented counter-clockwise.
     * @param lineString
     * @return 
     */
    private static LineString getCounterClockWise(final LineString lineString) {
        final Coordinate c0 = lineString.getCoordinateN(0);
        final Coordinate c1 = lineString.getCoordinateN(1);
        final Coordinate c2 = lineString.getCoordinateN(2);
        if (CGAlgorithms.computeOrientation(c0, c1, c2) == CGAlgorithms.COUNTERCLOCKWISE) {
            return lineString;
        } else {
            return (LineString) lineString.reverse();
        }
    }

    /**
     * Return a polygon oriented clockwise
     * @param polygon
     * @return 
     */
    private static Polygon getClockWise(final Polygon polygon) {
        final LinearRing shell = GF.createLinearRing(getClockWise(
                polygon.getExteriorRing()).getCoordinates());
        final int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = GF.createLinearRing(getCounterClockWise(
                    polygon.getInteriorRingN(i)).getCoordinates());
        }
        return GF.createPolygon(shell, holes);
    }

     /**
     * Return a polygon oriented counter-clockwise
     * @param polygon
     * @return 
     */
    private static Polygon getCounterClockWise(final Polygon polygon) {
        final LinearRing shell = GF.createLinearRing(getCounterClockWise(polygon.getExteriorRing()).getCoordinates());
        final int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = GF.createLinearRing(getClockWise(
                    polygon.getInteriorRingN(i)).getCoordinates());
        }
        return GF.createPolygon(shell, holes);
    }

    /**
     * Create a polygon corresponding to the wall
     *
     * @param beginPoint
     * @param endPoint
     * @param hight
     * @return
     */
    private static Polygon extrudeEdge(final Coordinate beginPoint,
            Coordinate endPoint, final double hight) {
        if (Double.isNaN(beginPoint.z)) {
            beginPoint.z = 0d;
        }
        if (Double.isNaN(endPoint.z)) {
            endPoint.z = 0d;
        }

        return GF.createPolygon(GF.createLinearRing(new Coordinate[]{
            beginPoint,
            new Coordinate(beginPoint.x, beginPoint.y, beginPoint.z
            + hight),
            new Coordinate(endPoint.x, endPoint.y, endPoint.z
            + hight), endPoint, beginPoint}), null);
    }

    /**
     * Translate the LineString according a specified hight.
     * @param ring
     * @param hight
     * @return a coordinate array translate according the input hight
     */
    private static Coordinate[] translate(final LineString ring, final double hight) {
        final Coordinate[] src = ring.getCoordinates();
        final Coordinate[] dst = new Coordinate[src.length];
        for (int i = 0; i < src.length; i++) {
            if (Double.isNaN(src[i].z)) {
                src[i].z = 0d;
            }
            dst[i] = new Coordinate(src[i].x, src[i].y, src[i].z + hight);
        }
        return dst;
    }    
}
