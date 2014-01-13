/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
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
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Compute the minimum bounding circle of a geometry. For more information,
 * see {@link MinimumBoundingCircle}.
 *
 * @author Erwan Bocher
 */
public class ST_BoundingCircle extends DeterministicScalarFunction {

    public ST_BoundingCircle() {
        addProperty(PROP_REMARKS, "Compute the minimum bounding circle of a geometry");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeBoundingCircle";
    }

    /**
     * Computes the bounding circle
     *
     * @param geometry
     * @return
     */
    public static Geometry computeBoundingCircle(Geometry geometry) {
        return new MinimumBoundingCircle(geometry).getCircle();
    }
}
