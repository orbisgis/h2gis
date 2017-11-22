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

package org.h2gis.functions.spatial.create;

import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Compute the minimum bounding circle center of a geometry
 * @author Nicolas Fortin
 */
public class ST_BoundingCircleCenter extends DeterministicScalarFunction {

    /**
     * Constructor
     */
    public ST_BoundingCircleCenter() {
        addProperty(PROP_REMARKS, "Compute the minimum bounding circle center of a geometry." +
                "This function is more precise than the conjunction of ST_CENTROID and ST_BoundingCircle");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getCircumCenter";
    }

    /**
     * Compute the minimum bounding circle center of a geometry
     * @param geometry Any geometry
     * @return Minimum bounding circle center point
     */
    public static Point getCircumCenter(Geometry geometry) {
        if(geometry == null || geometry.getNumPoints() == 0) {
            return null;
        }
        return geometry.getFactory().createPoint(new MinimumBoundingCircle(geometry).getCentre());
    }
}
