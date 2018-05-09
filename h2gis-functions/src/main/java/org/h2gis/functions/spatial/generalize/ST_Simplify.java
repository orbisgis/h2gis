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

package org.h2gis.functions.spatial.generalize;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.h2gis.api.DeterministicScalarFunction;

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
        if(geometry == null){
            return null;
        }
        return DouglasPeuckerSimplifier.simplify(geometry, distance);
    }
}
