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

package org.h2gis.functions.spatial.properties;

import org.locationtech.jts.geom.Geometry;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * ST_YMin returns the minimal y-value of the given geometry.
 *
 * @author Adam Gouge
 */
public class ST_YMin extends DeterministicScalarFunction {

    public ST_YMin() {
        addProperty(PROP_REMARKS, "Returns the minimal y-value of the given geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getMinY";
    }

    /**
     * Returns the minimal y-value of the given geometry.
     *
     * @param geom Geometry
     * @return The minimal y-value of the given geometry, or null if the geometry is null.
     */
    public static Double getMinY(Geometry geom) {
        if (geom != null) {
            return geom.getEnvelopeInternal().getMinY();
        } else {
            return null;
        }
    }
}
