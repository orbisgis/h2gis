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

package org.h2gis.functions.spatial.affine_transformations;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * ST_Scale scales the given geometry by multiplying the coordinates by the
 * indicated scale factors.
 *
 * @author Adam Gouge
 */
public class ST_Scale extends DeterministicScalarFunction {

    public ST_Scale() {
        addProperty(PROP_REMARKS, "Scales the given geometry by " +
                "multiplying the coordinates by the indicated scale factors");
    }

    @Override
    public String getJavaStaticMethod() {
        return "scale";
    }

    /**
     * Scales the given geometry by multiplying the coordinates by the
     * indicated x and y scale factors, leaving the z-coordinate untouched.
     *
     * @param geom    Geometry
     * @param xFactor x scale factor
     * @param yFactor y scale factor
     * @return The geometry scaled by the given x and y scale factors
     */
    public static Geometry scale(Geometry geom, double xFactor, double yFactor) {
        return scale(geom, xFactor, yFactor, 1.0);
    }

    /**
     * Scales the given geometry by multiplying the coordinates by the
     * indicated x, y and z scale factors.
     *
     * @param geom    Geometry
     * @param xFactor x scale factor
     * @param yFactor y scale factor
     * @param zFactor z scale factor
     * @return The geometry scaled by the given x, y and z scale factors
     */
    public static Geometry scale(Geometry geom, double xFactor, double yFactor, double zFactor) {
        if (geom != null) {
            Geometry scaledGeom = geom.copy();
            for (Coordinate c : scaledGeom.getCoordinates()) {
                c.setOrdinate(Coordinate.X, c.getOrdinate(Coordinate.X) * xFactor);
                c.setOrdinate(Coordinate.Y, c.getOrdinate(Coordinate.Y) * yFactor);
                c.setOrdinate(Coordinate.Z, c.getOrdinate(Coordinate.Z) * zFactor);
            }
            return scaledGeom;
        } else {
            return null;
        }
    }
}
