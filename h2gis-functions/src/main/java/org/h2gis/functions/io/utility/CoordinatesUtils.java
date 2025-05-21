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

package org.h2gis.functions.io.utility;

import org.locationtech.jts.geom.*;

/**
 * This utility class provides some useful methods related to JTS {@link org.locationtech.jts.geom.Coordinate} objects.
 *
 * @author Erwan Bocher
 */
public final class CoordinatesUtils {

        /**
         * Interpolates a z value (linearly) between the two coordinates.
         *
         * @param firstCoordinate first coordinate
         * @param lastCoordinate last coordinate
         * @param toBeInterpolated coordinate to interpolate
         * @return z value
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

        public static boolean contains(Coordinate[] coords, Coordinate coord) {
                for (Coordinate coordinate : coords) {
                        if (Double.isNaN(coord.getZ())) {
                                return coordinate.equals(coord);
                        } else {
                                return coordinate.equals3D(coord);
                        }
                }
                return false;
        }

        /**
         * Checks if a coordinate array contains a specific coordinate.
         *
         * The equality is done only in 2D (z values are not checked).
         *
         * @param coords coordinate array
         * @param coord coordinate to check
         * @return true if the coordinate is found
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
         * Check if a coordinate array contains a specific coordinate.
         *
         * The equality is done in 3D (z values ARE checked).
         *
         * @param coords coordinate array
         * @param coord coordinate
         * @return true if the coordinate is found
         */
        public static boolean contains3D(Coordinate[] coords, Coordinate coord) {
                for (Coordinate coordinate : coords) {
                        if (coordinate.equals3D(coord)) {
                                return true;
                        }
                }
                return false;
        }

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
         * Find the furthest coordinate in a geometry from a base coordinate
         *
         * @param base {@link Coordinate}
         * @param coords {@link Coordinate}
         * @return the base coordinate and the target coordinate
         */
        public static Coordinate[] getFurthestCoordinate(Coordinate base, Coordinate[] coords) {
                double distanceMax = Double.MIN_VALUE;
                Coordinate farCoordinate = null;
                for (Coordinate coord : coords) {
                        double distance = coord.distance(base);
                        if (distance > distanceMax) {
                                distanceMax = distance;
                                farCoordinate = coord;
                        }
                }

                if (farCoordinate != null) {
                        return new Coordinate[]{base, farCoordinate};
                } else {
                        return null;
                }
        }

        /**
         * Computes the length of a linestring specified by a sequence of points.
         * if a coordinate has a NaN z return 0.
         *
         * @param pts
         * the points specifying the linestring
         * @return the length of the linestring
         */
        public static double length3D(CoordinateSequence pts) {
                // optimized for processing CoordinateSequences
                int n = pts.size();
                if (n <= 1) {
                        return 0.0;
                }

                double len = 0.0;

                Coordinate p = new Coordinate();
                pts.getCoordinate(0, p);
                double x0 = p.x;
                double y0 = p.y;
                double z0 = p.getZ();

                if (Double.isNaN(z0)) {
                        return 0.0;
                }

                for (int i = 1; i < n; i++) {
                        pts.getCoordinate(i, p);

                        double x1 = p.x;
                        double y1 = p.y;
                        double z1 = p.getZ();
                        if (Double.isNaN(z1)) {
                                return 0.0;
                        }
                        double dx = x1 - x0;
                        double dy = y1 - y0;
                        double dz = z1 - z0;

                        len += Math.sqrt(dx * dx + dy * dy + dz * dz);
                        x0 = x1;
                        y0 = y1;
                        z0 = z1;
                }
                return len;
        }

        /**
         * Returns the 3D length of the geometry
         *
         *
         * @param geom {@link Geometry}
         * @return length in 3D
         */
        public static double length3D(Geometry geom) {
                double sum = 0;
                int size = geom.getNumGeometries();
                for (int i = 0; i < size; i++) {
                        Geometry subGeom = geom.getGeometryN(i);
                        if (subGeom instanceof Polygon) {
                                sum += length3D((Polygon) subGeom);
                        } else if (subGeom instanceof LineString) {
                                sum += length3D((LineString) subGeom);
                        }
                }
                return sum;
        }

        /**
         * Returns the 3D perimeter of a line string.
         *
         * @param lineString {@link LineString}
         * @return length in 3D
         */
        public static double length3D(LineString lineString) {
                return length3D(lineString.getCoordinateSequence());
        }

        /**
         * Returns the 3D perimeter of a polygon
         *
         * @param polygon {@link Polygon}
         * @return length in 3D
         */
        public static double length3D(Polygon polygon) {
                double len = 0.0;
                len += length3D(polygon.getExteriorRing().getCoordinateSequence());
                for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                        len += length3D(polygon.getInteriorRingN(i));
                }
                return len;
        }

        /**
         * Private constructor for utility class.
         */
        private CoordinatesUtils() {
        }
}
