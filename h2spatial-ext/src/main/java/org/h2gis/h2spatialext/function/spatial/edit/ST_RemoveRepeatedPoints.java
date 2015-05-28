/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Remove duplicated points on a geometry
 *
 * @author Erwan Bocher
 */
public class ST_RemoveRepeatedPoints extends DeterministicScalarFunction {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    public ST_RemoveRepeatedPoints() {
        addProperty(PROP_REMARKS, "Returns a version of the given geometry with duplicated points removed.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "removeRepeatedPoints";
    }

    /**
     * Returns a version of the given geometry with duplicated points removed.
     *
     * @param geometry
     * @return
     */
    public static Geometry removeRepeatedPoints(Geometry geometry) {
        return removeDuplicateCoordinates(geometry);
    }

    /**
     * Removes duplicated points within a geometry.
     *
     * @param geom
     * @return
     */
    public static Geometry removeDuplicateCoordinates(Geometry geom) {
        if(geom ==null){
            return null;
        }
        else if (geom.isEmpty()) {
            return geom;
        } else if (geom instanceof Point || geom instanceof MultiPoint) {
            return geom;
        } else if (geom instanceof LineString) {
            return removeDuplicateCoordinates((LineString) geom);
        } else if (geom instanceof MultiLineString) {
            return removeDuplicateCoordinates((MultiLineString) geom);
        } else if (geom instanceof Polygon) {
            return removeDuplicateCoordinates((Polygon) geom);
        } else if (geom instanceof MultiPolygon) {
            return removeDuplicateCoordinates((MultiPolygon) geom);
        } else if (geom instanceof GeometryCollection) {
            return removeDuplicateCoordinates((GeometryCollection) geom);
        }
        return null;
    }

    /**
     * Removes duplicated coordinates within a LineString.
     *
     * @param g
     * @return
     */
    public static LineString removeDuplicateCoordinates(LineString g) {
        Coordinate[] coords = CoordinateArrays.removeRepeatedPoints(g.getCoordinates());
        return FACTORY.createLineString(coords);
    }

    /**
     * Removes duplicated coordinates within a linearRing.
     *
     * @param g
     * @return
     */
    public static LinearRing removeDuplicateCoordinates(LinearRing g) {
        Coordinate[] coords = CoordinateArrays.removeRepeatedPoints(g.getCoordinates());
        return FACTORY.createLinearRing(coords);
    }

    /**
     * Removes duplicated coordinates in a MultiLineString.
     *
     * @param g
     * @return
     */
    public static MultiLineString removeDuplicateCoordinates(MultiLineString g) {
        ArrayList<LineString> lines = new ArrayList<LineString>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            LineString line = (LineString) g.getGeometryN(i);
            lines.add(removeDuplicateCoordinates(line));
        }
        return FACTORY.createMultiLineString(GeometryFactory.toLineStringArray(lines));
    }

    /**
     * Removes duplicated coordinates within a Polygon.
     *
     * @param poly
     * @return
     */
    public static Polygon removeDuplicateCoordinates(Polygon poly) {
        Coordinate[] shellCoords = CoordinateArrays.removeRepeatedPoints(poly.getExteriorRing().getCoordinates());
        LinearRing shell = FACTORY.createLinearRing(shellCoords);
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            Coordinate[] holeCoords = CoordinateArrays.removeRepeatedPoints(poly.getInteriorRingN(i).getCoordinates());
            holes.add(FACTORY.createLinearRing(holeCoords));
        }
        return FACTORY.createPolygon(shell, GeometryFactory.toLinearRingArray(holes));
    }

    /**
     * Removes duplicated coordinates within a MultiPolygon.
     *
     * @param g
     * @return
     */
    public static MultiPolygon removeDuplicateCoordinates(MultiPolygon g) {
        ArrayList<Polygon> polys = new ArrayList<Polygon>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Polygon poly = (Polygon) g.getGeometryN(i);
            polys.add(removeDuplicateCoordinates(poly));
        }
        return FACTORY.createMultiPolygon(GeometryFactory.toPolygonArray(polys));
    }

    /**
     * Removes duplicated coordinates within a GeometryCollection
     *
     * @param g
     * @return
     */
    public static GeometryCollection removeDuplicateCoordinates(GeometryCollection g) {
        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Geometry geom = g.getGeometryN(i);
            geoms.add(removeDuplicateCoordinates(geom));
        }
        return FACTORY.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
    }
}
