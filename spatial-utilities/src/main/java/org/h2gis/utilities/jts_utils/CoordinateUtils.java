/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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
package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;

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
     * Private constructor for utility class.
     */
    private CoordinateUtils() {
    }
}
