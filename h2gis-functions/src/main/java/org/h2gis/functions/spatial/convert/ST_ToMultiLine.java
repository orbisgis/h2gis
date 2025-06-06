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
 * ST_ToMultiLine constructs a MultiLineString from the given geometry's
 * coordinates. Returns MULTILINESTRING EMPTY for geometries of dimension 0.
 *
 * @author Erwan Bocher, CNRS
 * @author Adam Gouge
 */
public class ST_ToMultiLine extends DeterministicScalarFunction {

    public ST_ToMultiLine() {
        addProperty(PROP_REMARKS, "Constructs a MultiLineString from the given " +
                "geometry's coordinates.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Constructs a MultiLineString from the given geometry's coordinates.
     *
     * @param geom Geometry input geometry
     * @return A MultiLineString constructed from the given geometry's coordinates
     *
     */
    public static MultiLineString execute(Geometry geom) throws SQLException {
        if (geom != null) {
            if (geom.getDimension() > 0) {
                final List<LineString> lineStrings = new LinkedList<LineString>();
                toMultiLineString(geom, lineStrings);
                return geom.getFactory().createMultiLineString(
                        lineStrings.toArray(new LineString[0]));
            } else {
                return geom.getFactory().createMultiLineString(null);
            }
        } else {
            return null;
        }
    }

    private static void toMultiLineString(final Geometry geometry,
                                          final List<LineString> lineStrings) throws SQLException {
        if (geometry instanceof LineString) {
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
        int size = geometryCollection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            toMultiLineString(geometryCollection.getGeometryN(i), lineStrings);
        }
    }
}
