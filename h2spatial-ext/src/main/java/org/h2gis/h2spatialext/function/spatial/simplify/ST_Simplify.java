/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.simplify;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Returns a simplified version of the given geometry using the Douglas-Peuker
 * algorithm.
 *
 * @author Erwan Bocher
 */
public class ST_Simplify extends DeterministicScalarFunction {

    public ST_Simplify() {
        addProperty(PROP_REMARKS, "Returns a simplified version of the given geometry using the Douglas-Peuker algorithm.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "simplify";
    }

    /**
     * Simplify the geometry using the douglad peucker algorithm.
     * 
     * @param geometry
     * @param distance
     * @return 
     */
    public static Geometry simplify(Geometry geometry, double distance) {
        return DouglasPeuckerSimplifier.simplify(geometry, distance);
    }
}
