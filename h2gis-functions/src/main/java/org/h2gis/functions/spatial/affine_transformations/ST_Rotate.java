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
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * ST_Rotate rotates a geometry by a given angle (in radians) about the
 * geometry's center.
 *
 * @author Adam Gouge
 */
public class ST_Rotate extends DeterministicScalarFunction {

    public ST_Rotate() {
        addProperty(PROP_REMARKS, "Rotates a geometry by a given angle (in" +
                "radians) about the geometry's center.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "rotate";
    }

    /**
     * Rotates a geometry by a given angle (in radians) about the center
     * of the geometry's envelope.
     *
     * @param geom  Geometry
     * @param theta Angle
     * @return The geometry rotated about the center of its envelope
     */
    public static Geometry rotate(Geometry geom, double theta) {
        if (geom != null) {
            Coordinate center = geom.getEnvelopeInternal().centre();
            return rotate(geom, theta, center.x, center.y);
        } else {
            return null;
        }
    }

    /**
     * Rotates a geometry by a given angle (in radians) about the specified
     * point.
     *
     * @param geom  Geometry
     * @param theta Angle
     * @param point The point about which to rotate
     * @return The geometry rotated by theta about the given point
     */
    public static Geometry rotate(Geometry geom, double theta, Point point) {
        return rotate(geom, theta, point.getX(), point.getY());
    }

    /**
     * Rotates a geometry by a given angle (in radians) about the specified
     * point at (x0, y0).
     *
     * @param geom  Geometry
     * @param theta Angle
     * @param x0    x-coordinate of point about which to rotate
     * @param y0    y-coordinate of point about which to rotate
     * @return The geometry rotated by theta about (x0, y0)
     */
    public static Geometry rotate(Geometry geom, double theta, double x0, double y0) {
        if (geom != null) {
            return AffineTransformation.rotationInstance(theta, x0, y0).transform(geom);
        } else {
            return null;
        }
    }
}
