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
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.distance.GeometryLocation;
import java.sql.SQLException;
import java.util.ArrayList;
import org.h2gis.drivers.utility.CoordinatesUtils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_AddPoint extends DeterministicScalarFunction {

    private static final GeometryFactory FACTORY = new GeometryFactory();
    public static final double PRECISION = 10E-6;

    public ST_AddPoint(){
        addProperty(PROP_REMARKS, "Adds a point to a geometry. \n"
                + "A tolerance could be set to snap the point to the geometry." );        
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "addPoint";
    }

    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex.
     * A default distance 10E-6 is used to snap the input point.
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
        }
        else if(geometry instanceof Point){
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
            if (!CoordinatesUtils.contains2D(coords, coord)) {
                Coordinate[] ret = new Coordinate[coords.length + 1];
                System.arraycopy(coords, 0, ret, 0, index + 1);
                ret[index + 1] = coord;
                System.arraycopy(coords, index + 1, ret, index + 2, coords.length
                        - (index + 1));
                return FACTORY.createLineString(ret);
            }
            return null;
        } else {
            return null;
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
        LinearRing inserted = insertVertexInLinearRing(polygon.getExteriorRing(), vertexPoint, tolerance);
        if (inserted != null) {
            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < holes.length; i++) {
                holes[i] = FACTORY.createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
            }
            Polygon ret = FACTORY.createPolygon(inserted, holes);

            if (!ret.isValid()) {
                throw new SQLException("Geometry not valid");
            }

            return ret;
        }

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            inserted = insertVertexInLinearRing(polygon.getInteriorRingN(i), vertexPoint, tolerance);
            if (inserted != null) {
                LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
                for (int h = 0; h < holes.length; h++) {
                    if (h == i) {
                        holes[h] = inserted;
                    } else {
                        holes[h] = FACTORY.createLinearRing(polygon.getInteriorRingN(h).getCoordinates());
                    }
                }

                Polygon ret = FACTORY.createPolygon(FACTORY.createLinearRing(polygon.getExteriorRing().getCoordinates()), holes);

                if (!ret.isValid()) {
                    throw new SQLException("Geometry not valid");
                }
                return ret;
            }
        }
        return null;
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
            if (!CoordinatesUtils.contains2D(coords, coord)) {
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
