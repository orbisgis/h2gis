/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatial.internal.function.spatial.properties;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import org.h2gis.h2spatialapi.DeterministicScalarFunction;

import java.sql.SQLException;

/**
 * Returns a LinearRing instance or Null if parameter is not a Polygon. {@link org.h2gis.h2spatial.internal.function.spatial.properties.ST_NumInteriorRings}
 *
 * @author Nicolas Fortin
 */
public class ST_InteriorRingN extends DeterministicScalarFunction {
    private static final String OUT_OF_BOUNDS_ERR_MESSAGE =
            "Interior ring index out of range. Must be between 1 and ST_NumInteriorRings.";

    /**
     * Default constructor
     */
    public ST_InteriorRingN() {
        addProperty(PROP_REMARKS, "Returns interior ring number n from a Polygon. " +
                "Use ST_NumInteriorRings to retrieve the total number of interior rings.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getInteriorRing";
    }

    /**
     * @param geometry Polygon
     * @param n        Index of interior ring number n in [1-N]
     * @return Interior ring number n or NULL if parameter is null.
     * @throws SQLException
     */
    public static LineString getInteriorRing(Geometry geometry, Integer n) throws SQLException {
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            if (n >= 1 && n <= polygon.getNumInteriorRing()) {
                return polygon.getInteriorRingN(n - 1);
            } else {
                throw new SQLException(OUT_OF_BOUNDS_ERR_MESSAGE);
            }
        } else {
            return null;
        }
    }
}
