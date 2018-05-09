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

package org.h2gis.functions.spatial.distance;

import org.locationtech.jts.geom.*;
import org.h2gis.api.DeterministicScalarFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * ST_ClosestCoordinate computes the closest coordinate(s) contained in the
 * given geometry starting from the given point, using the 2D distance. If the
 * coordinate is unique, it is returned as a POINT. If it is not, then all
 * closest coordinates are returned in a MULTIPOINT.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class ST_ClosestCoordinate extends DeterministicScalarFunction {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public ST_ClosestCoordinate() {
        addProperty(PROP_REMARKS, "Computes the closest coordinate(s) contained in the " +
                "given geometry starting from the given point, using the 2D distance.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getFurthestCoordinate";
    }

    /**
     * Computes the closest coordinate(s) contained in the given geometry starting
     * from the given point, using the 2D distance.
     *
     * @param point Point
     * @param geom  Geometry
     * @return The closest coordinate(s) contained in the given geometry starting from
     *         the given point, using the 2D distance
     */
    public static Geometry getFurthestCoordinate(Point point, Geometry geom) {
        if (point == null || geom == null) {
            return null;
        }
        double minDistance = Double.POSITIVE_INFINITY;
        Coordinate pointCoordinate = point.getCoordinate();
        Set<Coordinate> closestCoordinates = new HashSet<Coordinate>();
        for (Coordinate c : geom.getCoordinates()) {
            double distance = c.distance(pointCoordinate);
            if (Double.compare(distance, minDistance) == 0) {
                closestCoordinates.add(c);
            }
            if (Double.compare(distance, minDistance) < 0) {
                minDistance = distance;
                closestCoordinates.clear();
                closestCoordinates.add(c);
            }
        }
        if (closestCoordinates.size() == 1) {
            return GEOMETRY_FACTORY.createPoint(closestCoordinates.iterator().next());
        }
        return GEOMETRY_FACTORY.createMultiPoint(
                closestCoordinates.toArray(new Coordinate[closestCoordinates.size()]));
    }
}
