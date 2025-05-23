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

package org.h2gis.functions.spatial.edit;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateUtils;
import org.locationtech.jts.geom.*;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Remove duplicated points on a geometry
 *
 * @author Erwan Bocher CNRS
 */
public class ST_RemoveRepeatedPoints extends DeterministicScalarFunction {

    public ST_RemoveRepeatedPoints() {
        addProperty(PROP_REMARKS, "Returns a version of the given geometry with duplicated points removed.\n"
                + "If the tolerance parameter is provided, vertices within the tolerance of one another will be considered the same for the purposes of removal. ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "removeRepeatedPoints";
    }

    /**
     * Returns a version of the given geometry with duplicated points removed.
     *
     * @param geometry {@link Geometry}
     * @return Geometry without repeated points
     */
    public static Geometry removeRepeatedPoints(Geometry geometry) throws SQLException, SQLException {
        return removeDuplicateCoordinates(geometry, 0);
    }
    
     /**
     * Returns a version of the given geometry with duplicated points removed.
     *
     * @param geometry {@link Geometry}
     * @param tolerance distance to delete the coordinates
     * @return Geometry without repeated points
     */
    public static Geometry removeRepeatedPoints(Geometry geometry, double tolerance) throws SQLException {
        return removeDuplicateCoordinates(geometry, tolerance);
    }

    /**
     * Removes duplicated points within a geometry.
     *
     * @param geom {@link Geometry}
     * @param tolerance distance to delete the coordinates
     * @return Geometry without repeated points
     */
    public static Geometry removeDuplicateCoordinates(Geometry geom, double tolerance) throws SQLException {
        if (geom == null) {
            return null;
        } else if (geom.isEmpty()) {
            return geom;
        } else if (geom instanceof Point) {
            return geom;
        } else if (geom instanceof MultiPoint) {
            return geom;
        } else if (geom instanceof LineString) {
            return removeDuplicateCoordinates((LineString) geom, tolerance);
        } else if (geom instanceof MultiLineString) {
            return removeDuplicateCoordinates((MultiLineString) geom, tolerance);
        } else if (geom instanceof Polygon) {
            return removeDuplicateCoordinates((Polygon) geom, tolerance);
        } else if (geom instanceof MultiPolygon) {
            return removeDuplicateCoordinates((MultiPolygon) geom, tolerance);
        } else if (geom instanceof GeometryCollection) {
            return removeDuplicateCoordinates((GeometryCollection) geom, tolerance);
        }
        return null;
    }
    
    
    
    /**
     * Removes duplicated coordinates within a LineString.
     *
     * @param linestring {@link LineString}
     * @param tolerance distance to delete the coordinates
     * @return Geometry without repeated points
     */
    public static LineString removeDuplicateCoordinates(LineString linestring, double tolerance) throws SQLException {
        Coordinate[] coords = CoordinateUtils.removeRepeatedCoordinates(linestring.getCoordinates(), tolerance, false);
        if(coords.length<2){  
            throw new SQLException("Not enough coordinates to build a new LineString.\n Please adjust the tolerance");
        }
        return linestring.getFactory().createLineString(coords);
    }

    /**
     * Removes duplicated coordinates within a linearRing.
     *
     * @param linearRing {@link LinearRing}
     * @param tolerance to delete the coordinates
     * @return Geometry without repeated points
     */
    public static LinearRing removeDuplicateCoordinates(LinearRing linearRing, double tolerance) {
        Coordinate[] coords = CoordinateUtils.removeRepeatedCoordinates(linearRing.getCoordinates(), tolerance, true);
        return linearRing.getFactory().createLinearRing(coords);
    }

    /**
     * Removes duplicated coordinates in a MultiLineString.
     *
     * @param multiLineString {@link MultiLineString}
     * @param tolerance to delete the coordinates
     * @return Geometry without repeated points
     */
    public static MultiLineString removeDuplicateCoordinates(MultiLineString multiLineString, double tolerance) throws SQLException {
        ArrayList<LineString> lines = new ArrayList<LineString>();
        int size = multiLineString.getNumGeometries();
        for (int i = 0; i < size; i++) {
            LineString line = (LineString) multiLineString.getGeometryN(i);
            lines.add(removeDuplicateCoordinates(line, tolerance));
        }
        return multiLineString.getFactory().createMultiLineString(GeometryFactory.toLineStringArray(lines));
    }

    /**
     * Removes duplicated coordinates within a Polygon.
     *
     * @param polygon the input polygon
     * @param tolerance to delete the coordinates
     * @return Geometry without repeated points
     */
    public static Polygon removeDuplicateCoordinates(Polygon polygon, double tolerance) throws SQLException {
        GeometryFactory factory = polygon.getFactory();
        Coordinate[] shellCoords = CoordinateUtils.removeRepeatedCoordinates(polygon.getExteriorRing().getCoordinates(),tolerance,true);
        LinearRing shell = factory.createLinearRing(shellCoords);
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Coordinate[] holeCoords = CoordinateUtils.removeRepeatedCoordinates(polygon.getInteriorRingN(i).getCoordinates(), tolerance, true);
            if (holeCoords.length < 4) {
                throw new SQLException("Not enough coordinates to build a new LinearRing.\n Please adjust the tolerance");
            }
            holes.add(factory.createLinearRing(holeCoords));
        }
        return factory.createPolygon(shell, GeometryFactory.toLinearRingArray(holes));
    }

    /**
     * Removes duplicated coordinates within a MultiPolygon.
     *
     * @param multiPolygon {@link MultiPolygon}
     * @param tolerance to delete the coordinates
     * @return Geometry without repeated points
     */
    public static MultiPolygon removeDuplicateCoordinates(MultiPolygon multiPolygon, double tolerance) throws SQLException {
        ArrayList<Polygon> polys = new ArrayList<Polygon>();
        int size = multiPolygon.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Polygon poly = (Polygon) multiPolygon.getGeometryN(i);
            polys.add(removeDuplicateCoordinates(poly, tolerance));
        }
        return multiPolygon.getFactory().createMultiPolygon(GeometryFactory.toPolygonArray(polys));
    }

    /**
     * Removes duplicated coordinates within a GeometryCollection
     *
     * @param geometryCollection {@link GeometryCollection}
     * @param tolerance to delete the coordinates
     * @return Geometry without repeated points
     */
    public static GeometryCollection removeDuplicateCoordinates(GeometryCollection geometryCollection, double tolerance) throws SQLException {
        ArrayList<Geometry> geoms = new ArrayList<>();
        int size = geometryCollection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry geom = geometryCollection.getGeometryN(i);
            geoms.add(removeDuplicateCoordinates(geom, tolerance));
        }
        return geometryCollection.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
    }
}
