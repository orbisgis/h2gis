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

package org.h2gis.functions.spatial.topography;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector3D;
import org.h2gis.api.DeterministicScalarFunction;

import org.h2gis.utilities.jts_utils.CoordinateUtils;
import org.h2gis.utilities.jts_utils.TriMarkers;


/**
 * This function is used to compute the main slope direction on a triangle.
 * 
* @author Erwan Bocher
 */
public class ST_TriangleDirection extends DeterministicScalarFunction {

    private static final GeometryFactory gf = new GeometryFactory();

    public ST_TriangleDirection() {
        addProperty(PROP_REMARKS, "Compute the steepest vector director for a triangle\n"
                + "and represent it as a linestring");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeDirection";
    }

    /**
     * Compute the main slope direction
     * @param geometry
     * @return
     * @throws IllegalArgumentException
     */
    public static LineString computeDirection(Geometry geometry) throws IllegalArgumentException {
        if(geometry == null){
            return null;
        }
        // Convert geometry into triangle
        Triangle triangle = TINFeatureFactory.createTriangle(geometry);
        // Compute slope vector
        Vector3D normal = TriMarkers.getNormalVector(triangle);
        Vector3D vector = new Vector3D(normal.getX(), normal.getY(), 0).normalize();
        // Compute equidistant point of triangle's sides
        Coordinate inCenter = triangle.centroid();
        // Interpolate Z value
        inCenter.setOrdinate(2, Triangle.interpolateZ(inCenter, triangle.p0, triangle.p1, triangle.p2));
        // Project slope from triangle center to triangle borders
        final LineSegment[] sides = new LineSegment[] {new LineSegment(triangle.p0, triangle.p1),
                new LineSegment(triangle.p1, triangle.p2), new LineSegment(triangle.p2, triangle.p0)};
        Coordinate pointIntersection = null;
        double nearestIntersection = Double.MAX_VALUE;
        for(LineSegment side : sides) {
            Coordinate intersection  = CoordinateUtils.vectorIntersection(inCenter, vector, side.p0,
                    new Vector3D(side.p0,side.p1).normalize());
            double distInters = intersection == null ? Double.MAX_VALUE : side.distance(intersection);
            if(intersection != null && distInters < nearestIntersection) {
                pointIntersection = new Coordinate(intersection.x, intersection.y,
                        Triangle.interpolateZ(intersection, triangle.p0, triangle.p1, triangle.p2));
                nearestIntersection = distInters;
            }
        }
        if (pointIntersection != null) {
            return gf.createLineString(new Coordinate[]{inCenter, pointIntersection});
        }
        return gf.createLineString(new Coordinate[] {});
    }
    
}
