/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.properties;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Point;

/**
 * Get the first M(easure) coordinate
 * @author Nicolas Fortin
 * @author Erwan Bocher, CNRS
 */
public class ST_M extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_M() {
        addProperty(PROP_REMARKS, "Return the M coordinate of a Point");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * @param geometry Geometry instance
     * @return a M coordinate or null if null or empty geometry.
     */
    public static Double execute(Point geometry) {
        if (geometry == null) {
            return null;
        }
        double m = geometry.getCoordinate().getM();
        if (Double.isNaN(m)){
            return null;
        }
        return m;
    }
}
