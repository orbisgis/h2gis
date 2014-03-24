/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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

package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
     */
    public static Point createPoint(double x, double y, double z) throws SQLException {
        return GF.createPoint(new Coordinate(x, y, z));
    }
}
