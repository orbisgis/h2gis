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

package org.h2gis.functions.spatial.create;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * Class to create a polygon
 *
 * @author Erwan Bocher
 */
public class ST_MakePolygon extends DeterministicScalarFunction {

    public ST_MakePolygon() {
        addProperty(PROP_REMARKS, "Creates a Polygon formed by the given shell and optionally holes.\n"
                + "Input geometries must be closed Linestrings");
    }

    @Override
    public String getJavaStaticMethod() {
        return "makePolygon";
    }

    /**
     * Creates a Polygon formed by the given shell.
     *
     * @param shell Geometry
     * @return Polygon
     */
    public static Polygon makePolygon(Geometry shell) throws IllegalArgumentException {
        if(shell == null) {
            return null;
        }
        LinearRing outerLine = checkLineString(shell);
        return shell.getFactory().createPolygon(outerLine, null);
    }

    /**
     * Creates a Polygon formed by the given shell and holes.
     *
     * @param shell {@link Geometry}
     * @param holes {@link Geometry}
     * @return Polygon
     */
    public static Polygon makePolygon(Geometry shell, Geometry holes) throws IllegalArgumentException {
        if (shell == null) {
            return null;
        }
        LinearRing outerLine = checkLineString(shell);
        if (holes == null) {
            return shell.getFactory().createPolygon(outerLine, null);
        } else {
            final int size = holes.getNumGeometries();
            LinearRing[] interiorlinestrings = new LinearRing[size];
            for (int i = 0; i < size; i++) {
                interiorlinestrings[i] = checkLineString(holes.getGeometryN(i));
            }
            return shell.getFactory().createPolygon(outerLine, interiorlinestrings);
        }
    }

    /**
     * Check if a geometry is a linestring and if its closed.
     *
     * @param geometry {@link Geometry}
     * @return LinearRing
     */
    private static LinearRing checkLineString(Geometry geometry) throws IllegalArgumentException {
        if (geometry instanceof LinearRing) {
            return (LinearRing) geometry;

        } else if (geometry instanceof LineString) {
            LineString lineString = (LineString) geometry;
            if (lineString.isClosed()) {
                return geometry.getFactory().createLinearRing(lineString.getCoordinateSequence());
            } else {
                throw new IllegalArgumentException("The linestring must be closed.");
            }
        } else  {
            throw new IllegalArgumentException("Only support linestring.");
        }
    }
}
