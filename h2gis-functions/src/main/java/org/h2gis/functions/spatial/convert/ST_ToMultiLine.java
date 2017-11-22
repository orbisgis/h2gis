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

package org.h2gis.functions.spatial.convert;

import org.locationtech.jts.geom.*;
import org.h2gis.api.DeterministicScalarFunction;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * ST_ToMultiLine constructs a MultiLineString from the given geometry's
 * coordinates. Returns MULTILINESTRING EMPTY for geometries of dimension 0.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class ST_ToMultiLine extends DeterministicScalarFunction {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public ST_ToMultiLine() {
        addProperty(PROP_REMARKS, "Constructs a MultiLineString from the given " +
                "geometry's coordinates.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createMultiLineString";
    }

    /**
     * Constructs a MultiLineString from the given geometry's coordinates.
     *
     * @param geom Geometry
     * @return A MultiLineString constructed from the given geometry's coordinates
     * @throws SQLException
     */
    public static MultiLineString createMultiLineString(Geometry geom) throws SQLException {
        if (geom != null) {
            if (geom.getDimension() > 0) {
                final List<LineString> lineStrings = new LinkedList<LineString>();
                toMultiLineString(geom, lineStrings);
                return GEOMETRY_FACTORY.createMultiLineString(
                        lineStrings.toArray(new LineString[lineStrings.size()]));
            } else {
                return GEOMETRY_FACTORY.createMultiLineString(null);
            }
        } else {
            return null;
        }
    }

    private static void toMultiLineString(final Geometry geometry,
                                          final List<LineString> lineStrings) throws SQLException {
        if ((geometry instanceof Point) || (geometry instanceof MultiPoint)) {
            throw new SQLException("Found a point! Cannot create a MultiLineString.");
        } else if (geometry instanceof LineString) {
            toMultiLineString((LineString) geometry, lineStrings);
        } else if (geometry instanceof Polygon) {
            toMultiLineString((Polygon) geometry, lineStrings);
        } else if (geometry instanceof GeometryCollection) {
            toMultiLineString((GeometryCollection) geometry, lineStrings);
        }
    }

    private static void toMultiLineString(final LineString lineString,
                                   final List<LineString> lineStrings) {
        lineStrings.add(lineString);
    }

    private static void toMultiLineString(final Polygon polygon,
                                   final List<LineString> lineStrings) {
        lineStrings.add(polygon.getExteriorRing());
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            lineStrings.add(polygon.getInteriorRingN(i));
        }
    }

    private static void toMultiLineString(final GeometryCollection geometryCollection,
                                   final List<LineString> lineStrings) throws SQLException {
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            toMultiLineString(geometryCollection.getGeometryN(i), lineStrings);
        }
    }
}
