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
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_SimplifyPreserveTopology extends DeterministicScalarFunction {

    public ST_SimplifyPreserveTopology() {
        addProperty(PROP_REMARKS, "Simplifies a geometry and ensures that the result is a valid geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "simplyPreserve";
    }

    /**
     * Simplifies a geometry and ensures that the result is a valid geometry
     * having the same dimension and number of components as the input, and with
     * the components having the same topological relationship.
     *
     * @param geometry
     * @param distance
     * @return
     */
    public static Geometry simplyPreserve(Geometry geometry, double distance) {
        return TopologyPreservingSimplifier.simplify(geometry, distance);
    }
}
