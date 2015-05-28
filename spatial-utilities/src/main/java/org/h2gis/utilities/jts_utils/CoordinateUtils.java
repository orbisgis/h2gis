/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.math.Vector3D;


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
            z = cs[t].z;

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
     * @param firstCoordinate
     * @param lastCoordinate
     * @param toBeInterpolated
     * @return
     */
    public static double interpolate(Coordinate firstCoordinate, Coordinate lastCoordinate, Coordinate toBeInterpolated) {
        if (Double.isNaN(firstCoordinate.z)) {
            return Double.NaN;
        }
        if (Double.isNaN(lastCoordinate.z)) {
            return Double.NaN;
        }
        return firstCoordinate.z + (lastCoordinate.z - firstCoordinate.z) * firstCoordinate.distance(toBeInterpolated)
                / (firstCoordinate.distance(toBeInterpolated) + toBeInterpolated.distance(lastCoordinate));
    }

    /**
     * Checks if a coordinate array contains a specific coordinate.
     *
     * The equality is done only in 2D (z values are not checked).
     *
     * @param coords
     * @param coord
     * @return
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
            i = new Coordinate(p1.x + k * v1.getX(), p1.y + k * v1.getY(), p1.z + k * v1.getZ());
            if(new LineSegment(p1, new Coordinate(p1.x + v1.getX(), p1.y + v1.getY())).projectionFactor(i) < 0 ||
                    new LineSegment(p2, new Coordinate(p2.x + v2.getX(), p2.y + v2.getY())).projectionFactor(i) < 0) {
                return null;
            }
        }
        return i;
    }

    /**
     * Private constructor for utility class.
     */
    private CoordinateUtils() {
    }
}
