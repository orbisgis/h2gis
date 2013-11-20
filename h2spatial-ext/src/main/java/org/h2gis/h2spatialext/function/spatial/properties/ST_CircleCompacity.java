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

package org.h2gis.h2spatialext.function.spatial.properties;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatial.internal.type.SC_Polygon;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * ST_CircleCompacity computes the perimeter of a circle whose area is equal to the
 * given geometry's area, and returns the ratio of this computed perimeter to the given
 * geometry's perimeter.
 *
 * @author Adam Gouge
 */
public class ST_CircleCompacity extends DeterministicScalarFunction {

    public ST_CircleCompacity() {
        addProperty(PROP_REMARKS, "computes the perimeter of a circle whose area is " +
                "equal to the given geometry's area, and returns the ratio of " +
                "this computed perimeter to the given geometry's perimeter.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeCompacity";
    }

    /**
     * Computes the perimeter of a circle whose area is equal to the given geometry's
     * area, and returns the ratio of this computed perimeter to the given geometry's
     * perimeterReturns the dimension of the coordinates of the given geometry.
     *
     * @param geom Geometry
     * @return The ratio of the computed perimeter to the given geometry's perimeter,
     *         or null if the given geometry is not a Polygon.
     */
    public static Double computeCompacity(Geometry geom) {
        if (geom != null) {
            if (SC_Polygon.isPolygon(geom)) {
                final double circleRadius = Math.sqrt(geom.getArea() / Math.PI);
                final double circleCurcumference = 2 * Math.PI * circleRadius;
                return circleCurcumference / geom.getLength();
            }
        }
        return null;
    }
}
