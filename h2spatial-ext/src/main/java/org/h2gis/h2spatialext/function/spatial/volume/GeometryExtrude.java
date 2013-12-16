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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Erwan Bocher
 * @author Thomas Leduc
 */
public class GeometryExtrude {

    private static final GeometryFactory GF = new GeometryFactory();
    public int WALL = 2;
    public int FLOOR = 0;
    public int ROOF = 1;

    private GeometryExtrude() {
    }

    /**
     * This method transform a polygon to collection of geometries that contains
     * wall, floor and roof using a hight parameter
     *
     * @param polygon
     * @param high
     * @return
     */
    public HashMap<Integer, Geometry> extrudePolygon(Polygon polygon, double hight) {
        HashMap<Integer, Geometry> extrudedCollection = new HashMap<Integer, Geometry>();
        //Add the floor
        extrudedCollection.put(FLOOR, getClockWise(polygon));

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
        extrudedCollection.put(WALL, GF.createMultiPolygon(walls.toArray(new Polygon[walls.size()])));

        //We create the roof        
        final LinearRing upperShell = translate(polygon.getExteriorRing(), hight);
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = translate(polygon.getInteriorRingN(i), hight);
        }
        extrudedCollection.put(ROOF, GF.createPolygon(upperShell, holes));


        return extrudedCollection;
    }

    public Polygon extrudeLineString() {
        return null;
    }

    private LineString getClockWise(final LineString lineString) {
        final Coordinate c0 = lineString.getCoordinateN(0);
        final Coordinate c1 = lineString.getCoordinateN(1);
        final Coordinate c2 = lineString.getCoordinateN(2);

        if (CGAlgorithms.computeOrientation(c0, c1, c2) == CGAlgorithms.CLOCKWISE) {
            return lineString;
        } else {
            return (LineString) lineString.reverse();
        }
    }

    private LineString getCounterClockWise(final LineString lineString) {
        final Coordinate c0 = lineString.getCoordinateN(0);
        final Coordinate c1 = lineString.getCoordinateN(1);
        final Coordinate c2 = lineString.getCoordinateN(2);
        if (CGAlgorithms.computeOrientation(c0, c1, c2) == CGAlgorithms.COUNTERCLOCKWISE) {
            return lineString;
        } else {
            return (LineString) lineString.reverse();
        }
    }

    private Polygon getClockWise(final Polygon polygon) {
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

    private Polygon getCounterClockWise(final Polygon polygon) {
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
     * @param high
     * @return
     */
    private Polygon extrudeEdge(final Coordinate beginPoint,
            Coordinate endPoint, final double high) {
        if (Double.isNaN(beginPoint.z)) {
            beginPoint.z = 0d;
        }
        if (Double.isNaN(endPoint.z)) {
            endPoint.z = 0d;
        }

        return GF.createPolygon(GF.createLinearRing(new Coordinate[]{
            beginPoint,
            new Coordinate(beginPoint.x, beginPoint.y, beginPoint.z
            + high),
            new Coordinate(endPoint.x, endPoint.y, endPoint.z
            + high), endPoint, beginPoint}), null);
    }

    private LinearRing translate(final LineString ring, final double high) {
        final Coordinate[] src = ring.getCoordinates();
        final Coordinate[] dst = new Coordinate[src.length];
        for (int i = 0; i < src.length; i++) {
            if (Double.isNaN(src[i].z)) {
                src[i].z = 0d;
            }
            dst[i] = new Coordinate(src[i].x, src[i].y, src[i].z + high);
        }
        return GF.createLinearRing(dst);
    }
}
