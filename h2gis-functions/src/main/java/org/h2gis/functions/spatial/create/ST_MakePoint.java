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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.h2gis.api.DeterministicScalarFunction;

import java.sql.SQLException;

/**
 * ST_MakePoint constructs POINT from two or three doubles.
 *
 * @author Adam Gouge
 */
public class ST_MakePoint extends DeterministicScalarFunction {

    private static final GeometryFactory GF = new GeometryFactory();

    public ST_MakePoint() {
        addProperty(PROP_REMARKS, "Constructs POINT from two or three doubles");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createPoint";
    }

    /**
     * Constructs POINT from two doubles.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The POINT constructed from the given coordinatesk
     * @throws java.sql.SQLException
     */
    public static Point createPoint(double x, double y) throws SQLException {
        return createPoint(x, y, Coordinate.NULL_ORDINATE);
    }

    /**
     * Constructs POINT from three doubles.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param z Z-coordinate
     * @return The POINT constructed from the given coordinates
     * @throws SQLException
     */
    public static Point createPoint(double x, double y, double z) throws SQLException {
        return GF.createPoint(new Coordinate(x, y, z));
    }
}
