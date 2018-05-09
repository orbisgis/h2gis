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

package org.h2gis.functions.spatial.volume;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import java.util.ArrayList;
import org.h2gis.functions.spatial.edit.ST_UpdateZ.UpdateZCoordinateSequenceFilter;

/**
 * This class is used to extrude a polygon or a linestring to a set of walls,
 * roof, or floor using a height.
 *
 * @author Erwan Bocher
 */
public class GeometryExtrude {

    

    private GeometryExtrude() {
    }
    
    /**
     * Extrude the polygon as a collection of geometries
     * The output geometryCollection contains the floor, the walls and the roof.
     * @param polygon
     * @param height
     * @return 
     */
    public static GeometryCollection extrudePolygonAsGeometry(Polygon polygon, double height){
        GeometryFactory factory = polygon.getFactory();
        Geometry[] geometries = new Geometry[3];        
        //Extract floor
        //We process the exterior ring 
        final LineString shell = getClockWise(polygon.getExteriorRing());
        ArrayList<Polygon> walls = new ArrayList<Polygon>();
        for (int i = 1; i < shell.getNumPoints(); i++) {
            walls.add(extrudeEdge(shell.getCoordinateN(i - 1), shell.getCoordinateN(i), height, factory));
        }

        final int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            final LineString hole = getCounterClockWise(polygon.getInteriorRingN(i));
            for (int j = 1; j < hole.getNumPoints(); j++) {
                walls.add(extrudeEdge(hole.getCoordinateN(j - 1),
                        hole.getCoordinateN(j), height, factory));
            }
            holes[i] = factory.createLinearRing(hole.getCoordinateSequence());            
        }
        
        geometries[0]= factory.createPolygon(factory.createLinearRing(shell.getCoordinateSequence()), holes);
        geometries[1]= factory.createMultiPolygon(walls.toArray(new Polygon[walls.size()]));
        geometries[2]= extractRoof(polygon, height);
        return polygon.getFactory().createGeometryCollection(geometries);
    }
    
     /**
     * Extrude the linestring as a collection of geometries
     * The output geometryCollection contains the floor, the walls and the roof.
     * @param lineString
     * @param height
     * @return 
     */
    public static GeometryCollection extrudeLineStringAsGeometry(LineString lineString, double height){
        Geometry[] geometries = new Geometry[3];        
        GeometryFactory factory = lineString.getFactory();
        //Extract the walls        
        Coordinate[] coords = lineString.getCoordinates();
        Polygon[] walls = new Polygon[coords.length - 1];
        for (int i = 0; i < coords.length - 1; i++) {
            walls[i] = extrudeEdge(coords[i], coords[i + 1], height, factory);
        }
        lineString.apply(new TranslateCoordinateSequenceFilter(0));
        geometries[0]= lineString;
        geometries[1]=  factory.createMultiPolygon(walls);        
        geometries[2]= extractRoof(lineString, height);
        return factory.createGeometryCollection(geometries);
    }
    
    /**
     * Extract the linestring "roof".
     *
     * @param lineString
     * @param height
     * @return
     */
    public static Geometry extractRoof(LineString lineString, double height) {
        LineString result = lineString.copy();
        result.apply(new TranslateCoordinateSequenceFilter(height));
        return result;
    }
    
    
    
    /**
     * Extract the walls from a polygon
     * @param polygon
     * @param height
     * @return 
     */
    public static MultiPolygon extractWalls(Polygon polygon, double height){
        GeometryFactory factory = polygon.getFactory();
        //We process the exterior ring 
        final LineString shell = getClockWise(polygon.getExteriorRing());

        ArrayList<Polygon> walls = new ArrayList<Polygon>();
        for (int i = 1; i < shell.getNumPoints(); i++) {
            walls.add(extrudeEdge(shell.getCoordinateN(i - 1), shell.getCoordinateN(i), height, factory));
        }

        // We create the walls  for all holes 
        int nbOfHoles = polygon.getNumInteriorRing();
        for (int i = 0; i < nbOfHoles; i++) {
            final LineString hole = getCounterClockWise(polygon.getInteriorRingN(i));
            for (int j = 1; j < hole.getNumPoints(); j++) {
                walls.add(extrudeEdge(hole.getCoordinateN(j - 1),
                        hole.getCoordinateN(j), height, factory));
            }
        }
        return polygon.getFactory().createMultiPolygon(walls.toArray(new Polygon[walls.size()]));
    }
    
    /**
     * Extract the roof of a polygon
     * 
     * @param polygon
     * @param height
     * @return 
     */
    public static Polygon extractRoof(Polygon polygon, double height) {
        GeometryFactory factory = polygon.getFactory();
        Polygon roofP =  polygon.copy();
        roofP.apply(new TranslateCoordinateSequenceFilter(height));
        final LinearRing shell = factory.createLinearRing(getCounterClockWise(roofP.getExteriorRing()).getCoordinates());
        final int nbOfHoles = roofP.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = factory.createLinearRing(getClockWise(
                    roofP.getInteriorRingN(i)).getCoordinates());
        }
        return factory.createPolygon(shell, holes);
    }

    /**
     * Extrude the LineString as a set of walls.
     * @param lineString
     * @param height
     * @return
     */
    public static MultiPolygon extractWalls(LineString lineString, double height) {
        GeometryFactory factory = lineString.getFactory();
        //Extract the walls        
        Coordinate[] coords = lineString.getCoordinates();
        Polygon[] walls = new Polygon[coords.length - 1];
        for (int i = 0; i < coords.length - 1; i++) {
            walls[i] = extrudeEdge(coords[i], coords[i + 1], height, factory);
        }
        return lineString.getFactory().createMultiPolygon(walls);
    }

    /**
     * Reverse the LineString to be oriented clockwise.
     * All NaN z values are replaced by a zero value.
     * @param lineString
     * @return 
     */
    private static LineString getClockWise(final LineString lineString) {
        final Coordinate c0 = lineString.getCoordinateN(0);
        final Coordinate c1 = lineString.getCoordinateN(1);
        final Coordinate c2 = lineString.getCoordinateN(2);
        lineString.apply(new UpdateZCoordinateSequenceFilter(0, 3));
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
        lineString.apply(new UpdateZCoordinateSequenceFilter(0, 3));
        if (CGAlgorithms.computeOrientation(c0, c1, c2) == CGAlgorithms.COUNTERCLOCKWISE) {
            return lineString;
        } else {
            return (LineString) lineString.reverse();
        }
    }

    /**
     * Reverse the polygon to be oriented clockwise
     * @param polygon
     * @return 
     */
    private static Polygon extractFloor(final Polygon polygon) {
        GeometryFactory factory = polygon.getFactory();
        final LinearRing shell = factory.createLinearRing(getClockWise(
                polygon.getExteriorRing()).getCoordinates());
        final int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = factory.createLinearRing(getCounterClockWise(
                    polygon.getInteriorRingN(i)).getCoordinates());
        }
        return factory.createPolygon(shell, holes);
    }     

    /**
     * Create a polygon corresponding to the wall.
     * 
     *
     * @param beginPoint
     * @param endPoint
     * @param height
     * @return
     */
    private static Polygon extrudeEdge(final Coordinate beginPoint,
            Coordinate endPoint, final double height, GeometryFactory factory) {
        beginPoint.z = Double.isNaN(beginPoint.z) ? 0 : beginPoint.z;
        endPoint.z = Double.isNaN(endPoint.z) ? 0 : endPoint.z;        
        return factory.createPolygon(new Coordinate[]{
            beginPoint,
            new Coordinate(beginPoint.x, beginPoint.y, beginPoint.z
            + height),
            new Coordinate(endPoint.x, endPoint.y, endPoint.z
            + height), endPoint, beginPoint});
    }

     
    
    /**
     * Translate a geometry to a specific z value added to each vertexes.
     *
     */
    public static class TranslateCoordinateSequenceFilter implements CoordinateSequenceFilter {

        private boolean done = false;
        private final double z;

        public TranslateCoordinateSequenceFilter(double z) {
            this.z = z;
        }

        @Override
        public boolean isGeometryChanged() {
            return true;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public void filter(CoordinateSequence seq, int i) {
            Coordinate coord = seq.getCoordinate(i);
            double currentZ = coord.z;
            if (!Double.isNaN(currentZ)) {
                seq.setOrdinate(i, 2, currentZ + z);
            }
            else{
                 seq.setOrdinate(i, 2, z);
            }
            if (i == seq.size()) {
                done = true;
            }
        }
    }
}
