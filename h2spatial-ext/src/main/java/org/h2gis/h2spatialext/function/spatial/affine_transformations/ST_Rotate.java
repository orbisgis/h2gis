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

package org.h2gis.h2spatialext.function.spatial.affine_transformations;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
        Coordinate center = geom.getEnvelopeInternal().centre();
        return rotate(geom, theta, center.x, center.y);
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
