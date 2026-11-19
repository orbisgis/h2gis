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
import org.locationtech.jts.geom.*;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Erwan Bocher, CNRS
 */
public class ST_AddPoint extends DeterministicScalarFunction {

    public static final double PRECISION = 10E-6;

    public ST_AddPoint() {
        addProperty(PROP_REMARKS, "Adds a point to a LINESTRING. \n"
                + "Without index, the point is appended to the LINESTRING.\n"
                + "With an index (0-based), the point replaces the vertex at this index,"
                + " an index equal to the number of points appends the point.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "addPoint";
    }

    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex. A default distance 10E-6 is used to snap the input point.
     *
     * @param geometry {@link Geometry}
     * @param point {@link Point}
     * @return Geometry with the new point
     */
    public static Geometry addPoint(Geometry geometry, Point point) throws SQLException {
        if(geometry == null || point == null){
            return null;
        }
        if(point.isEmpty()){
            return geometry;
        }
        if(geometry.isEmpty()){
            return geometry;
        }

        if(geometry.getSRID()!=point.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        int position = geometry.getNumPoints()+1;
        GeometryFactory factory = geometry.getFactory();
        if (geometry instanceof LineString) {
            return insertVertexInLineString((LineString) geometry, point, position, factory);
        }
        throw new SQLException("First argument must be a LINESTRING, use ST_InsertPoint for the other geometries");
    }


    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex.
     *
     * @param geometry {@link Geometry}
     * @param point {@link Point}
     * @param position index
     * @return same geometry if the vertex cannot be inserted
     * @throws SQLException If the vertex can be inserted but it makes the
     * geometry to be in an invalid shape
     */
    public static Geometry addPoint(Geometry geometry, Point point, int position) throws SQLException {
        if(geometry == null || point == null){
            return null;
        }
        if(geometry.isEmpty()||point.isEmpty()){
            return geometry;
        }
        if(geometry.getSRID()!=point.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }

        if (position < 0){
            throw  new SQLException("Point index must start at 0");
        }

        int numGeom = geometry.getNumPoints();
        if(position>numGeom){
            throw new SQLException("The point index is out of range (0.." + numGeom
                    + "): found " + position);
        }
        GeometryFactory factory = geometry.getFactory();
        if (geometry instanceof LineString) {
            return insertVertexInLineString((LineString) geometry, point, position, factory);
        }
        throw new SQLException("First argument must be a LINESTRING, use ST_InsertPoint for the other geometries");
    }

    /**
     * Adds a Point into a MultiPoint geometry.
     *
     * @param g {@link MultiPoint}
     * @param vertexPoint {@link Point}
     * @param position vertex position
     * @param factory {@link GeometryFactory}
     * @return Geometry with the new vertex
     */
    private static Geometry insertVertexInMultipoint(MultiPoint g, Point vertexPoint, int position, GeometryFactory factory) throws SQLException {
        ArrayList<Point> geoms = new ArrayList<Point>();
        boolean added =false;
        int size = g.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Point geom ;
            if(i==position){
                geoms.add(vertexPoint);
                added=true;
            }else {
                geoms.add((Point) g.getGeometryN(i));
            }
        }
        if(!added) {
            geoms.add(vertexPoint);
        }
        return factory.createMultiPoint(geoms.toArray(new Point[0]));
    }

    /**
     * Inserts a vertex into a LineString with a given tolerance.
     *
     * @param lineString {@link LineString}
     * @param vertexPoint {@link Point}
     * @param factory {@link GeometryFactory}
     * @return Geometry with the new vertex
     */
    private static LineString insertVertexInLineString(LineString lineString, Point vertexPoint, GeometryFactory factory) throws SQLException {
        CoordinateSequence coordSeq = lineString.getCoordinateSequence();
        return factory.createLineString(addCoordinate(coordSeq, vertexPoint.getCoordinate(),coordSeq.size()+1 ));
    }

    /**
     * Inserts a vertex into a LineString with a given tolerance.
     *
     * @param lineString {@link LineString}
     * @param vertexPoint {@link Point}
     * @param factory {@link GeometryFactory}
     * @return Geometry with the new vertex
     */
    private static LineString insertVertexInLineString(LineString lineString, Point vertexPoint, int position, GeometryFactory factory) throws SQLException {
        return factory.createLineString(addCoordinate(lineString.getCoordinateSequence(), vertexPoint.getCoordinate(),position ));
    }
    /**
     * Put a coordinate in the coordinates array of a LineString.
     * If the position is lower than the number of coordinates, the coordinate
     * at this position is replaced, otherwise the coordinate is appended.
     * The Z and M values of the new coordinate follow the dimension of the
     * LineString, 0 when the point has no Z or M value.
     *
     * @param coorseq {@link CoordinateSequence}
     * @param point the new coordinate
     * @param position vertex position (0-based)
     * @return Coordinate array
     */
    public static  Coordinate[] addCoordinate(CoordinateSequence coorseq,   Coordinate point, int position) {
        Coordinate coord;
        double z = Double.isNaN(point.getZ()) ? 0 : point.getZ();
        double m = Double.isNaN(point.getM()) ? 0 : point.getM();
        if (coorseq.hasZ() && coorseq.hasM()) {
            coord = new CoordinateXYZM(point.getX(), point.getY(), z, m);
        } else if (coorseq.hasZ()) {
            coord = new Coordinate(point.getX(), point.getY(), z);
        } else if (coorseq.hasM()) {
            coord = new CoordinateXYM(point.getX(), point.getY(), m);
        } else {
            coord = new CoordinateXY(point.getX(), point.getY());
        }
        // copy the coordinates, the input geometry must not be modified
        Coordinate[] coordinates = CoordinateArrays.copyDeep(coorseq.toCoordinateArray());
        if (position < coordinates.length) {
            coordinates[position] = coord;
            return coordinates;
        }
        Coordinate[] destArray = new Coordinate[coordinates.length + 1];
        System.arraycopy(coordinates, 0, destArray, 0, coordinates.length);
        destArray[coordinates.length] = coord;
        return destArray;
    }
}