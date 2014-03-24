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

package org.h2gis.h2spatialext.function.spatial.distance;

import com.vividsolutions.jts.geom.*;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * ST_FurthestCoordinate computes the furthest coordinate(s) contained in the
 * given geometry starting from the given point, using the 2D distance. If the
 * coordinate is unique, it is returned as a POINT. If it is not, then all
 * furthest coordinates are returned in a MULTIPOINT.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class ST_FurthestCoordinate extends DeterministicScalarFunction {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public ST_FurthestCoordinate() {
        addProperty(PROP_REMARKS, "Computes the furthest coordinate(s) contained in the " +
                "given geometry starting from the given point, using the 2D distance.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getFurthestCoordinate";
    }

    /**
     * Computes the furthest coordinate(s) contained in the given geometry starting
     * from the given point, using the 2D distance.
     *
     * @param point Point
     * @param geom  Geometry
     * @return The furthest coordinate(s) contained in the given geometry starting from
     *         the given point, using the 2D distance
     */
    public static Geometry getFurthestCoordinate(Point point, Geometry geom) {
        if (point == null || geom == null) {
            return null;
        }
        double maxDistance = Double.NEGATIVE_INFINITY;
        Coordinate pointCoordinate = point.getCoordinate();
        Set<Coordinate> furthestCoordinates = new HashSet<Coordinate>();
        for (Coordinate c : geom.getCoordinates()) {
            double distance = c.distance(pointCoordinate);
            if (Double.compare(distance, maxDistance) == 0) {
                furthestCoordinates.add(c);
            }
            if (Double.compare(distance, maxDistance) > 0) {
                maxDistance = distance;
                furthestCoordinates.clear();
                furthestCoordinates.add(c);
            }
        }
        if (furthestCoordinates.size() == 1) {
            return GEOMETRY_FACTORY.createPoint(furthestCoordinates.iterator().next());
        }
        return GEOMETRY_FACTORY.createMultiPoint(
                furthestCoordinates.toArray(new Coordinate[furthestCoordinates.size()]));
    }
}
