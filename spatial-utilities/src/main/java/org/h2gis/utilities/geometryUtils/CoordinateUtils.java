/**
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
package org.h2gis.utilities.geometryUtils;

import com.vividsolutions.jts.geom.*;

/**
 * This utility class provides some useful methods related to JTS {@link Coordinate} objects.
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
     * Returns the 3D length of the given geometry.
     *
     * @param geom Geometry
     * @return The 3D length of the given geometry
     */
    public static double length3D(Geometry geom) {
        double sum = 0;
        for (int i = 0; i < geom.getNumGeometries(); i++) {
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
     * Returns the 3D perimeter of the given polygon.
     *
     * @param polygon Polygon
     * @return The 3D perimeter of the given polygon
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
     * Returns the 3D perimeter of the given LineString.
     *
     * @param lineString LineString
     * @return The 3D perimeter of the given LineString
     */
    public static double length3D(LineString lineString) {
        return length3D(lineString.getCoordinateSequence());
    }

    /**
     * Computes the length of a LineString specified by a sequence of
     * coordinates, returning 0 if there is a coordinate with a NaN z-value.
     *
     * @param pts The coordinate sequence
     * @return The length of the corresponding LineString
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
        double z0 = p.z;

        if (Double.isNaN(z0)) {
            return 0.0;
        }

        for (int i = 1; i < n; i++) {
            pts.getCoordinate(i, p);

            double x1 = p.x;
            double y1 = p.y;
            double z1 = p.z;
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
     * Private constructor for utility class.
     */
    private CoordinateUtils() {
    }
}
