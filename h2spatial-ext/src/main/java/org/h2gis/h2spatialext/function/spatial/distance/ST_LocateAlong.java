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

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

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
        return GEOMETRY_FACTORY.createMultiPoint(
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
