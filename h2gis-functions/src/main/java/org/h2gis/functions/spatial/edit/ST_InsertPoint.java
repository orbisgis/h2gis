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
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.distance.GeometryLocation;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This function insert a point on geometry looking for the nearest coordinate segment
 *
 * @author Erwan Bocher, CNRS, 2020
 */
public class ST_InsertPoint extends DeterministicScalarFunction {

    public static final double PRECISION = 10E-6;

    public ST_InsertPoint() {
        addProperty(PROP_REMARKS, "Insert a point to a geometry. \n"
                + "A tolerance could be set to snap the point to the geometry.\n" +
                "If the point cannot be inserted it returns the input geometry");
    }

    @Override
    public String getJavaStaticMethod() {
        return "insertPoint";
    }

    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex. A default distance 10E-6 is used to snap the input point.
     *
     * @param geometry {@link Geometry}
     * @param point {@link Point}
     * @return Geometry with the new point
     */
    public static Geometry insertPoint(Geometry geometry, Point point) throws SQLException {
        return insertPoint(geometry, point, PRECISION);
    }

    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex.
     *
     * @param geometry {@link Geometry}
     * @param point {@link Point}
     * @param tolerance tolerance
     * @return Null if the vertex cannot be inserted
     * @throws SQLException If the vertex can be inserted but it makes the
     * geometry to be in an invalid shape
     */
    public static Geometry insertPoint(Geometry geometry, Point point, double tolerance) throws SQLException {
        if(geometry == null || point == null){
            return null;
        }
        if(point.isEmpty()||geometry.isEmpty()){
            return geometry;
        }
        if(geometry.getSRID()!=point.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        GeometryFactory factory = geometry.getFactory();
        if(geometry instanceof Point){
            throw new SQLException("Cannot insert a point on " + " : " + geometry.getGeometryType());
        }
        else if (geometry instanceof MultiPoint) {
            return insertVertexInMultipoint(geometry, point, factory);
        } else if (geometry instanceof LineString) {
            return insertVertexInLineString((LineString) geometry, point, tolerance, factory);
        } else if (geometry instanceof MultiLineString) {
            int size = geometry.getNumGeometries();
            LineString[] linestrings = new LineString[size];
            boolean any = false;
            for (int i = 0; i < size; i++) {
                LineString line = (LineString) geometry.getGeometryN(i);

                LineString inserted = insertVertexInLineString(line, point, tolerance, factory);
                if (inserted != null) {
                    linestrings[i] = inserted;
                    any = true;
                } else {
                    linestrings[i] = line;
                }
            }
            if (any) {
                return factory.createMultiLineString(linestrings);
            } else {
                return null;
            }
        } else if (geometry instanceof Polygon) {
            return insertVertexInPolygon((Polygon) geometry, point, tolerance, factory);
        } else if (geometry instanceof MultiPolygon) {
            Polygon[] polygons = new Polygon[geometry.getNumGeometries()];
            boolean any = false;
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) geometry.getGeometryN(i);
                Polygon inserted = insertVertexInPolygon(polygon, point, tolerance, factory);
                if (inserted != null) {
                    any = true;
                    polygons[i] = inserted;
                } else {
                    polygons[i] = polygon;
                }
            }
            if (any) {
                return factory.createMultiPolygon(polygons);
            } else {
                return null;
            }
        } else if (geometry instanceof GeometryCollection) {
            throw new SQLException("Cannot insert a point on " + " : " + geometry.getGeometryType());
        }
        throw new SQLException("Unknown geometry type" + " : " + geometry.getGeometryType());
    }

    /**
     * Adds a Point into a MultiPoint geometry.
     *
     * @param g {@link Geometry}
     * @param vertexPoint {@link Point}
     * @param factory {@link GeometryFactory}
     * @return geometry
     */
    private static Geometry insertVertexInMultipoint(Geometry g, Point vertexPoint, GeometryFactory factory) {
        ArrayList<Point> geoms = new ArrayList<Point>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Point geom = (Point) g.getGeometryN(i);
            geoms.add(geom);
        }
        geoms.add(factory.createPoint(new Coordinate(vertexPoint.getX(), vertexPoint.getY())));
        return factory.createMultiPoint(GeometryFactory.toPointArray(geoms));
    }

    /**
     * Inserts a vertex into a LineString with a given tolerance.
     *
     * @param lineString {@link LineString}
     * @param vertexPoint {@link Point}
     * @param tolerance tolerance
     * @param factory {@link GeometryFactory}
     * @return LineString with the new {@link Point}
     */
    private static LineString insertVertexInLineString(LineString lineString, Point vertexPoint,
                                                       double tolerance,  GeometryFactory factory) throws SQLException {
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
                return factory.createLineString(ret);
            }
            return null;
        } else {
            return lineString;
        }
    }

    /**
     * Adds a vertex into a Polygon with a given tolerance.
     *
     * @param polygon {@link Polygon}
     * @param vertexPoint {@link Point}
     * @param tolerance tolerance
     * @param factory {@link GeometryFactory}
     * @return Polygon with the new {@link Point}
     */
    private static Polygon insertVertexInPolygon(Polygon polygon,
                                                 Point vertexPoint, double tolerance,  GeometryFactory factory) throws SQLException {
        Polygon geom =polygon;
        LinearRing linearRing = factory.createLinearRing(polygon.getExteriorRing().getCoordinates());
        int index = -1;
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            double distCurr = computeDistance(polygon.getInteriorRingN(i),vertexPoint, tolerance);
            if (distCurr<tolerance){
                index = i;
            }
        }
        if(index==-1){
            //The point is a on the exterior ring.
            LinearRing inserted = insertVertexInLinearRing(linearRing, vertexPoint, tolerance, factory);
            if(inserted!=null){
                LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
                for (int i = 0; i < holes.length; i++) {
                    holes[i]= factory.createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
                }
                geom = factory.createPolygon(inserted, holes);
            }
        }
        else{
            //We add the vertex on the first hole
            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < holes.length; i++) {
                if (i == index) {
                    holes[i] = insertVertexInLinearRing(polygon.getInteriorRingN(i), vertexPoint, tolerance, factory);
                } else {
                    holes[i] = factory.createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
                }
            }
            geom = factory.createPolygon(linearRing, holes);
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
     * @param geometry {@link Geometry}
     * @param vertexPoint {@link Point}
     * @param tolerance tolerance
     * @return distance to snap the {@link Point}
     */
    private static double computeDistance(Geometry geometry, Point vertexPoint, double tolerance) {
        DistanceOp distanceOp = new DistanceOp(geometry, vertexPoint, tolerance);
        return distanceOp.distance();
    }

    /**
     * Adds a vertex into a LinearRing with a given tolerance.
     *
     * @param lineString {@link LineString}
     * @param vertexPoint {@link Point}
     * @param tolerance tolerance
     * @param factory {@link GeometryFactory}
     * @return linearRing with the new {@link Point}
     */
    private static LinearRing insertVertexInLinearRing(LineString lineString,
                                                       Point vertexPoint, double tolerance,  GeometryFactory factory) {
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
                return factory.createLinearRing(ret);
            }
            return null;
        } else {
            return null;
        }
    }
}