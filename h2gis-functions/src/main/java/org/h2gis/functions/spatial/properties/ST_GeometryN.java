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

import java.sql.SQLException;

/**
 * Return Geometry number n from the given GeometryCollection. Use {@link
 * org.h2gis.functions.spatial.properties.ST_NumGeometries}
 * to retrieve the total number of Geometries.
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class ST_GeometryN extends DeterministicScalarFunction {
    private static final String OUT_OF_BOUNDS_ERR_MESSAGE =
            "Geometry index out of range. Must be between 1 and ST_NumGeometries.";

    /**
     * Default constructor
     */
    public ST_GeometryN() {
        addProperty(PROP_REMARKS, "Returns Geometry number n from a GeometryCollection. " +
                "Use ST_NumGeometries to retrieve the total number of Geometries.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getGeometryN";
    }

    /**
     * Return Geometry number n from the given GeometryCollection.
     *
     * @param geometry GeometryCollection
     * @param n        Index of Geometry number n in [1-N]
     * @return Geometry number n or Null if parameter is null.
     * @throws SQLException
     */
    public static Geometry getGeometryN(Geometry geometry, Integer n) throws SQLException {
        if (geometry == null) {
            return null;
        }
        if (n >= 1 && n <= geometry.getNumGeometries()) {
            return geometry.getGeometryN(n - 1);
        } else {
            throw new SQLException(OUT_OF_BOUNDS_ERR_MESSAGE);
        }
    }
}
