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
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Compute the geometry length.
 * @author Nicolas Fortin
 */
public class ST_Length extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_Length() {
        addProperty(PROP_REMARKS, "Returns the 2D length of the geometry if it is a LineString or MultiLineString.\n"
                + " 0 is returned for other geometries");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getLength";
    }

    /**
     * @param geometry Geometry instance or 0
     * @return Geometry length for LineString or MultiLineString otherwise 0
     */
    public static Double getLength(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof LineString || geometry instanceof MultiLineString) {
            return geometry.getLength();
        }
        return 0.0d;
    }
}
