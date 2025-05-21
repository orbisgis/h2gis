/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.distance;

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

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
     * @param point Point point
     * @param geom  Geometry distance to
     * @return The furthest coordinate(s) contained in the given geometry starting from
     *         the given point, using the 2D distance
     */
    public static Geometry getFurthestCoordinate(Point point, Geometry geom) throws SQLException {
        if (point == null || geom == null) {
            return null;
        }
        if(point.isEmpty()||geom.isEmpty()){
            return null;
        }
        if(point.getSRID()!=geom.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
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
                furthestCoordinates.toArray(new Coordinate[0]));
    }
}
