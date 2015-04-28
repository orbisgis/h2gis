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
package org.h2gis.h2spatialext.function.spatial.topography;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.math.Vector3D;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

import org.h2gis.utilities.jts_utils.CoordinateUtils;
import org.h2gis.utilities.jts_utils.TriMarkers;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Vector;

/**
 * This function is used to compute the main slope direction on a triangle.
 * 
* @author Erwan Bocher
 */
public class ST_TriangleDirection extends DeterministicScalarFunction {

    private static GeometryFactory gf = new GeometryFactory();

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
        // Convert geometry into triangle
        Triangle triangle = TINFeatureFactory.createTriangle(geometry);
        // Compute slope vector
        Vector3D vector = TriMarkers.getSteepestVector(TriMarkers.getNormalVector(triangle), TINFeatureFactory.EPSILON);
        // Compute equidistant point of triangle's sides
        Coordinate inCenter = triangle.centroid();
        // Interpolate Z value
        inCenter.setOrdinate(2, Triangle.interpolateZ(inCenter, triangle.p0, triangle.p1, triangle.p2));
        // Project slope from triangle center to triangle borders
        final LineSegment[] sides = new LineSegment[] {new LineSegment(triangle.p0, triangle.p1),
                new LineSegment(triangle.p1, triangle.p2), new LineSegment(triangle.p2, triangle.p0)};
        Coordinate pointIntersection = null;
        for(LineSegment side : sides) {
            Coordinate intersection  = CoordinateUtils.vectorIntersection(inCenter, vector, side.p0,
                    new Vector3D(side.p0,side.p1).normalize());
            if(intersection != null && side.distance(intersection) < TINFeatureFactory.EPSILON) {
                pointIntersection = intersection;
                break;
            }
        }
        if (pointIntersection != null) {
            return gf.createLineString(new Coordinate[]{inCenter, pointIntersection});
        }
        return gf.createLineString(new Coordinate[] {});
    }
    
}
