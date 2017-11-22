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
 * Return TRUE if the provided geometry is a closed LINESTRING or
 * MULTILINESTRING, null otherwise.
 *
 * @author Nicolas Fortin
 */
public class ST_IsClosed extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_IsClosed() {
        addProperty(PROP_REMARKS, "Return TRUE if the provided geometry is " +
                "a closed LINESTRING or MULTILINESTRING, null otherwise.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "isClosed";
    }

    /**
     * @param geometry Geometry
     * @return True if the provided geometry is a closed LINESTRING or
     * MULTILINESTRING, null otherwise
     */
    public static Boolean isClosed(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof MultiLineString) {
            return ((MultiLineString) geometry).isClosed();
        } else if (geometry instanceof LineString) {
            return ((LineString) geometry).isClosed();
        }
        return null;
    }
}
