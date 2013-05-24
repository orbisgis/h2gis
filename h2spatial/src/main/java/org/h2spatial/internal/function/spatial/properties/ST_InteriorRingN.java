/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2spatial.internal.function.spatial.properties;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import org.h2spatialapi.ScalarFunction;

import java.sql.SQLException;

/**
 * Returns a LinearRing instance or Null if parameter is not a Geometry.
 * @author Nicolas Fortin
 */
public class ST_InteriorRingN implements ScalarFunction {
    private static final String OUT_OF_BOUNDS_ERR_MESSAGE = "ST_InteriorRingN index > ST_NumInteriorRings or index <= 0, Ring index must be in the range [1-NbRings]";

    @Override
    public String getJavaStaticMethod() {
        return "getInteriorRing";
    }

    @Override
    public Object getProperty(String propertyName) {
        if(propertyName.equals(ScalarFunction.PROP_DETERMINISTIC)) {
            return true;
        }
        return null;
    }

    /**
     * @param geometry Instance of Polygon
     * @param i Index of Interior ring [1-N]
     * @return LinearRing instance or Null if parameter is not a Geometry.
     */
    public static Geometry getInteriorRing(Geometry geometry,Integer i) throws SQLException {
        if(geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            if(i>=1 && i<=polygon.getNumInteriorRing()) {
                return polygon.getInteriorRingN(i-1);
            } else {
                throw new SQLException(OUT_OF_BOUNDS_ERR_MESSAGE);
            }
        } else {
            return null;
        }
    }
}
