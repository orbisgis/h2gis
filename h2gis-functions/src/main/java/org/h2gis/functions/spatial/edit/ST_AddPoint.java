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

package org.h2gis.functions.spatial.edit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.distance.GeometryLocation;
import java.sql.SQLException;
import java.util.ArrayList;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateUtils;

/**
 *
 * @author Erwan Bocher
 */
public class ST_AddPoint extends DeterministicScalarFunction {

    private static final GeometryFactory FACTORY = new GeometryFactory();
    public static final double PRECISION = 10E-6;

    public ST_AddPoint() {
        addProperty(PROP_REMARKS, "Adds a point to a geometry. \n"
                + "A tolerance could be set to snap the point to the geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "addPoint";
    }

    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex. A default distance 10E-6 is used to snap the input point.
     *
     * @param geometry
     * @param point
     * @return
     * @throws SQLException
     */
    public static Geometry addPoint(Geometry geometry, Point point) throws SQLException {
        return addPoint(geometry, point, PRECISION);
    }

    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex.
     *
     * @param geometry
     * @param point
     * @param tolerance
     * @return Null if the vertex cannot be inserted
     * @throws SQLException If the vertex can be inserted but it makes the
     * geometry to be in an invalid shape
     */
    public static Geometry addPoint(Geometry geometry, Point point, double tolerance) throws SQLException {
        if(geometry == null || point == null){
            return null;
        }
        if (geometry instanceof MultiPoint) {
            return insertVertexInMultipoint(geometry, point);
        } else if (geometry instanceof LineString) {
            return insertVertexInLineString((LineString) geometry, point, tolerance);
        } else if (geometry instanceof MultiLineString) {
            LineString[] linestrings = new LineString[geometry.getNumGeometries()];
            boolean any = false;
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                LineString line = (LineString) geometry.getGeometryN(i);

                LineString inserted = insertVertexInLineString(line, point, tolerance);
                if (inserted != null) {
                    linestrings[i] = inserted;
                    any = true;
                } else {
                    linestrings[i] = line;
                }
            }
            if (any) {
                return FACTORY.createMultiLineString(linestrings);
            } else {
                return null;
            }
        } else if (geometry instanceof Polygon) {
            return insertVertexInPolygon((Polygon) geometry, point, tolerance);
        } else if (geometry instanceof MultiPolygon) {
            Polygon[] polygons = new Polygon[geometry.getNumGeometries()];
            boolean any = false;
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) geometry.getGeometryN(i);
                Polygon inserted = insertVertexInPolygon(polygon, point, tolerance);
                if (inserted != null) {
                    any = true;
                    polygons[i] = inserted;
                } else {
                    polygons[i] = polygon;
                }
            }
            if (any) {
                return FACTORY.createMultiPolygon(polygons);
            } else {
                return null;
            }
        } else if (geometry instanceof Point) {
            return null;
        }
        throw new SQLException("Unknown geometry type" + " : " + geometry.getGeometryType());
    }

    /**
     * Adds a Point into a MultiPoint geometry.
     *
     * @param g
     * @param vertexPoint
     * @return
     */
    private static Geometry insertVertexInMultipoint(Geometry g, Point vertexPoint) {
        ArrayList<Point> geoms = new ArrayList<Point>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Point geom = (Point) g.getGeometryN(i);
            geoms.add(geom);
        }
        geoms.add(FACTORY.createPoint(new Coordinate(vertexPoint.getX(), vertexPoint.getY())));
        return FACTORY.createMultiPoint(GeometryFactory.toPointArray(geoms));
    }

    /**
     * Inserts a vertex into a LineString with a given tolerance.
     *
     * @param lineString
     * @param vertexPoint
     * @param tolerance
     * @return
     * @throws SQLException
     */
    private static LineString insertVertexInLineString(LineString lineString, Point vertexPoint,
            double tolerance) throws SQLException {
        GeometryLocation geomLocation = EditUtilities.getVertexToSnap(lineString, vertexPoint, tolerance);
        if (geomLocation != null) {
            Coordinate[] coords = lineString.getCoordinates();
            int index = geomLocation.getSegmentIndex();
            Coordinate coord = geomLocation.getCoordinate();
            if (!CoordinateUtils.contains2D(coords, coord)) {
                Coordinate[] ret = new Coordinate[coords.length + 1];
                System.arraycopy(coords, 0, ret, 0, index + 1);
                ret[index + 1] = coord;
                System.arraycopy(coords, index + 1, ret, index + 2, coords.length
                        - (index + 1));
                return FACTORY.createLineString(ret);
            }
            return null;
        } else {
            return lineString;
        }
    }

    /**
     * Adds a vertex into a Polygon with a given tolerance.
     *
     * @param polygon
     * @param vertexPoint
     * @param tolerance
     * @return
     * @throws SQLException
     */
    private static Polygon insertVertexInPolygon(Polygon polygon,
            Point vertexPoint, double tolerance) throws SQLException {        
        Polygon geom =polygon;
        LineString linearRing = polygon.getExteriorRing();
        int index = -1;
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            double distCurr = computeDistance(polygon.getInteriorRingN(i),vertexPoint, tolerance);
            if (distCurr<tolerance){
                index = i;
            }
        }        
        if(index==-1){
            //The point is a on the exterior ring.
            LinearRing inserted = insertVertexInLinearRing(linearRing, vertexPoint, tolerance);
            if(inserted!=null){
            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < holes.length; i++) {
                holes[i]= (LinearRing) polygon.getInteriorRingN(i);
            }
            geom = FACTORY.createPolygon(inserted, holes);
            }
        }
        else{
            //We add the vertex on the first hole
            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < holes.length; i++) {
                if (i == index) {
                    holes[i] = insertVertexInLinearRing(polygon.getInteriorRingN(i), vertexPoint, tolerance);
                } else {
                    holes[i] = (LinearRing) polygon.getInteriorRingN(i);
                }
            }
            geom = FACTORY.createPolygon((LinearRing) linearRing, holes);
        }       
        if(geom!=null){
        if (!geom.isValid()) {
            throw new SQLException("Geometry not valid");
        }
        }
        return geom;
    }

    /**
     * Return minimum distance between a geometry and a point.
     *
     * @param geometry
     * @param vertexPoint
     * @param tolerance
     * @return
     */
    private static double computeDistance(Geometry geometry, Point vertexPoint, double tolerance) {
        DistanceOp distanceOp = new DistanceOp(geometry, vertexPoint, tolerance);
        return distanceOp.distance();
    }

    /**
     * Adds a vertex into a LinearRing with a given tolerance.
     *
     * @param lineString
     * @param vertexPoint
     * @param tolerance
     * @return
     */
    private static LinearRing insertVertexInLinearRing(LineString lineString,
            Point vertexPoint, double tolerance) {
        GeometryLocation geomLocation = EditUtilities.getVertexToSnap(lineString, vertexPoint, tolerance);
        if (geomLocation != null) {
            Coordinate[] coords = lineString.getCoordinates();
            int index = geomLocation.getSegmentIndex();
            Coordinate coord = geomLocation.getCoordinate();
            if (!CoordinateUtils.contains2D(coords, coord)) {
                Coordinate[] ret = new Coordinate[coords.length + 1];
                System.arraycopy(coords, 0, ret, 0, index + 1);
                ret[index + 1] = coord;
                System.arraycopy(coords, index + 1, ret, index + 2, coords.length
                        - (index + 1));
                return FACTORY.createLinearRing(ret);
            }
            return null;
        } else {
            return null;
        }
    }
}
