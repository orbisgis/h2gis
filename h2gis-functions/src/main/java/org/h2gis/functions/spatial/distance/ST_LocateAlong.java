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
 * ST_LocateAlong returns a MULTIPOINT containing points along the line
 * segments of the given geometry matching the specified segment length
 * fraction and offset distance. A positive offset places the point to the left
 * of the segment (with the ordering given by Coordinate traversal); a negative
 * offset to the right. For areal elements, only exterior rings are supported.
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_LocateAlong extends DeterministicScalarFunction {
    
    public ST_LocateAlong() {
        addProperty(PROP_REMARKS, "Returns a MULTIPOINT containing points along " +
                "the line segments of the given geometry matching the specified " +
                "segment length fraction and offset distance. A positive offset " +
                "places the point to the left of the segment (with the ordering " +
                "given by Coordinate traversal); a negative offset to the right. " +
                "For areal elements, only exterior rings are supported.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "locateAlong";
    }

    /**
     * Returns a MULTIPOINT containing points along the line segments of the
     * given geometry matching the specified segment length fraction and offset
     * distance.
     *
     * @param geom Geometry
     * @param segmentLengthFraction Segment length fraction
     * @param offsetDistance Offset distance
     * @return A MULTIPOINT containing points along the line segments of the
     *         given geometry matching the specified segment length fraction and offset
     *         distance
     */
    public static MultiPoint locateAlong(Geometry geom,
                                         double segmentLengthFraction,
                                         double offsetDistance) {
        if (geom == null) {
            return null;
        }
        if (geom.getDimension() == 0) {
            return null;
        }
        Set<Coordinate> result = new HashSet<Coordinate>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Geometry subGeom = geom.getGeometryN(i);
            if (subGeom instanceof Polygon) {
                // Ignore hole
                result.addAll(computePoints(((Polygon) subGeom).getExteriorRing().getCoordinates(),
                        segmentLengthFraction, offsetDistance));
            } else if (subGeom instanceof LineString) {
                result.addAll(computePoints(subGeom.getCoordinates(),
                        segmentLengthFraction, offsetDistance));
            }
        }
        return geom.getFactory().createMultiPoint(
                result.toArray(new Coordinate[result.size()]));
    }

    private static Set<Coordinate> computePoints(Coordinate[] coords,
                                                 double segmentLengthFraction,
                                                 double offsetDistance) {
        Set<Coordinate> pointsToAdd = new HashSet<Coordinate>();
        for (int j = 0; j < coords.length - 1; j++) {
            LineSegment segment = new LineSegment(coords[j], coords[j + 1]);
            pointsToAdd.add(segment.pointAlongOffset(segmentLengthFraction, offsetDistance));
        }
        return pointsToAdd;
    }
}
