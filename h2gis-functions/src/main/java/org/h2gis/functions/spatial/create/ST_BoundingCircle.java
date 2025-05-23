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
import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.geom.Geometry;

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
     * @param geometry {@link Geometry}
     * @return geometry bounding circle
     */
    public static Geometry computeBoundingCircle(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        return new MinimumBoundingCircle(geometry).getCircle();
    }
}
