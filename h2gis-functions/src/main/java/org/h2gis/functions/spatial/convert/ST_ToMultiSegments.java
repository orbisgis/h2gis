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

package org.h2gis.functions.spatial.convert;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.*;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * ST_ToMultiSegments converts a geometry into a set of distinct segments
 * stored in a MultiLineString. Returns MULTILINESTRING EMPTY for geometries of
 * dimension 0.
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_ToMultiSegments extends DeterministicScalarFunction {

    public ST_ToMultiSegments() {
        addProperty(PROP_REMARKS, "Converts a geometry into a set of distinct " +
                "segments stored in a MultiLineString.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createSegments";
    }

    /**
     * Converts a geometry into a set of distinct segments stored in a
     * MultiLineString.
     *
     * @param geom Geometry
     * @return A MultiLineString of the geometry's distinct segments
     */
    public static MultiLineString createSegments(Geometry geom) throws SQLException {
        if (geom != null) {
            List<LineString> result;
            if (geom.getDimension() > 0) {
                result = new LinkedList<LineString>();
                createSegments(geom, result);
                return geom.getFactory().createMultiLineString(
                        result.toArray(new LineString[0]));
            } else {
                return geom.getFactory().createMultiLineString(null);
            }
        }
        return null;
    }

    private static void createSegments(final Geometry geom,
                                       final List<LineString> result) throws SQLException {
        if (geom instanceof LineString) {
            createSegments((LineString) geom, result);
        } else if (geom instanceof Polygon) {
            createSegments((Polygon) geom, result);
        } else if (geom instanceof GeometryCollection) {
            createSegments((GeometryCollection) geom, result);
        }
    }

    public static void createSegments(final LineString geom,
                                       final List<LineString> result) throws SQLException {
        Coordinate[] coords = CoordinateArrays.removeRepeatedPoints(geom.getCoordinates());
        for (int j = 0; j < coords.length - 1; j++) {
            LineString lineString = geom.getFactory().createLineString(
                    new Coordinate[]{coords[j], coords[j + 1]});
            result.add(lineString);
        }
    }

    private static void createSegments(final Polygon polygon,
                                       final List<LineString> result) throws SQLException {
        createSegments(polygon.getExteriorRing(), result);
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            createSegments(polygon.getInteriorRingN(i), result);
        }
    }

    private static void createSegments(final GeometryCollection geometryCollection,
                                       final List<LineString> result) throws SQLException {
        int size = geometryCollection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            createSegments(geometryCollection.getGeometryN(i), result);
        }
    }
}
