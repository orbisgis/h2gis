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

package org.h2gis.utilities.jts_utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector3D;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;


/**
 * Useful methods for JTS {@link Coordinate}s.
 *
 * @author Erwan Bocher
 */
public final class CoordinateUtils {

    /**
     * Determine the min and max "z" values in an array of Coordinates.
     *
     * @param cs The array to search.
     * @return An array of size 2, index 0 is min, index 1 is max.
     */
    public static double[] zMinMax(final Coordinate[] cs) {
        double zmin;
        double zmax;
        boolean validZFound = false;
        double[] result = new double[2];

        zmin = Double.NaN;
        zmax = Double.NaN;

        double z;

        for (int t = cs.length - 1; t >= 0; t--) {
            z = cs[t].getZ();

            if (!(Double.isNaN(z))) {
                if (validZFound) {
                    if (z < zmin) {
                        zmin = z;
                    }

                    if (z > zmax) {
                        zmax = z;
                    }
                } else {
                    validZFound = true;
                    zmin = z;
                    zmax = z;
                }
            }
        }
        result[0] = (zmin);
        result[1] = (zmax);
        return result;
    }

    /**
     * Interpolates a z value (linearly) between the two coordinates.
     *
     * @param firstCoordinate first coordinate
     * @param lastCoordinate last coordinate
     * @param toBeInterpolated coordinate to be interpolate
     * @return coordinate with z interpolated
     */
    public static double interpolate(Coordinate firstCoordinate, Coordinate lastCoordinate, Coordinate toBeInterpolated) {
        if (Double.isNaN(firstCoordinate.getZ())) {
            return Double.NaN;
        }
        if (Double.isNaN(lastCoordinate.getZ())) {
            return Double.NaN;
        }
        return firstCoordinate.getZ() + (lastCoordinate.getZ() - firstCoordinate.getZ()) * firstCoordinate.distance(toBeInterpolated)
                / (firstCoordinate.distance(toBeInterpolated) + toBeInterpolated.distance(lastCoordinate));
    }

    /**
     * Checks if a coordinate array contains a specific coordinate.
     *
     * The equality is done only in 2D (z values are not checked).
     *
     * @param coords array of coordinates
     * @param coord coordinate
     * @return true if the array contains the coordinate
     */
    public static boolean contains2D(Coordinate[] coords, Coordinate coord) {
        for (Coordinate coordinate : coords) {
            if (coordinate.equals2D(coord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compute intersection point of two vectors
     * @param p1 Origin point
     * @param v1 Direction from p1
     * @param p2 Origin point 2
     * @param v2 Direction of p2
     * @return Null if vectors are collinear or if intersection is done behind one of origin point
     */
    public static Coordinate vectorIntersection(Coordinate p1, Vector3D v1, Coordinate p2, Vector3D v2) {
        double delta;
        Coordinate i = null;
        // Cramer's rule for compute intersection of two planes
        delta = v1.getX() * (-v2.getY()) - (-v1.getY()) * v2.getX();
        if (delta != 0) {
            double k = ((p2.x - p1.x) * (-v2.getY()) - (p2.y - p1.y) * (-v2.getX())) / delta;
            // Fix precision problem with big decimal
            i = new Coordinate(p1.x + k * v1.getX(), p1.y + k * v1.getY(), p1.getZ() + k * v1.getZ());
            if(new LineSegment(p1, new Coordinate(p1.x + v1.getX(), p1.y + v1.getY())).projectionFactor(i) < 0 ||
                    new LineSegment(p2, new Coordinate(p2.x + v2.getX(), p2.y + v2.getY())).projectionFactor(i) < 0) {
                return null;
            }
        }
        return i;
    }
    
    /**
     * Remove dupliacted coordinates 
     * Note : This method doesn't preserve the topology of geometry
     * 
     * @param coords the input coordinates
     * @param closeRing is true the first coordinate is added at the end to close the array
     * @return the input coordinates without duplicates
     */
    public static Coordinate[] removeDuplicatedCoordinates(Coordinate[] coords, boolean closeRing) {
        LinkedHashSet<Coordinate> finalCoords = new LinkedHashSet<Coordinate>();
        Coordinate prevCoord = coords[0];
        finalCoords.add(prevCoord);
        Coordinate firstCoord = prevCoord;
        int nbCoords = coords.length;
        for (int i = 1; i < nbCoords; i++) {
            Coordinate currentCoord = coords[i];
            if (currentCoord.equals2D(prevCoord)) {
                continue;
            }
            finalCoords.add(currentCoord);
            prevCoord = currentCoord;
        }
        if (closeRing) {
            Coordinate[] coordsFinal = finalCoords.toArray(new Coordinate[0]);
            Coordinate[] closedCoords = Arrays.copyOf(coordsFinal, coordsFinal.length + 1);
            closedCoords[closedCoords.length-1] = firstCoord;
            return closedCoords;
        }
        return finalCoords.toArray(new Coordinate[0]);

    }
    
    
    /**
     * Remove repeated coordinates according a given tolerance
     * 
     * @param coords the input coordinates
     * @param tolerance to delete the coordinates
     * @param duplicateFirstLast false to delete the last coordinate 
     * if there are equals
     * @return the input coordinates without repeated coordinates
     */
    public static Coordinate[] removeRepeatedCoordinates(Coordinate[] coords, double tolerance, boolean duplicateFirstLast) {
        ArrayList<Coordinate> finalCoords = new ArrayList<Coordinate>();        
        Coordinate prevCoord = coords[0];
        finalCoords.add(prevCoord);
        Coordinate firstCoord =prevCoord;        
        int nbCoords = coords.length;
        for (int i = 1; i < nbCoords; i++) {
            Coordinate currentCoord = coords[i];
            if (currentCoord.distance(prevCoord) <= tolerance) {
                continue;
            }
            finalCoords.add(currentCoord);
            prevCoord = currentCoord;

        }
        if (!duplicateFirstLast) {
            if (firstCoord.distance(prevCoord) <= tolerance) {
                finalCoords.remove(finalCoords.size()-1);
            }
        }
        else{
            finalCoords.add(firstCoord);
        }
        return finalCoords.toArray(new Coordinate[0]);
        }

    /**
     *  Round a double value according a number of decimal places
     *
     * @param value the double value
     * @param places the number of decimal places
     * @return rounded value
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Private constructor for utility class.
     */
    private CoordinateUtils() {
    }
}
