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

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.*;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Erwan Bocher
 */
public class ST_AddPoint extends DeterministicScalarFunction {

    public static final double PRECISION = 10E-6;

    public ST_AddPoint() {
        addProperty(PROP_REMARKS, "Adds a point to a geometry. \n"
                + "An index to set the position of the point (0-based index).");
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
        if(geometry == null || point == null){
            return null;
        }
        if(geometry.getSRID()!=point.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        int position = geometry.getNumPoints()+1;
        GeometryFactory factory = geometry.getFactory();
        if (geometry instanceof LineString) {
            return insertVertexInLineString((LineString) geometry, point, position, factory);
        }
        throw new SQLException("First argument must be a LINESTRING");
    }


    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex.
     *
     * @param geometry
     * @param point
     * @param position
     * @return same geometry if the vertex cannot be inserted
     * @throws SQLException If the vertex can be inserted but it makes the
     * geometry to be in an invalid shape
     */
    public static Geometry addPoint(Geometry geometry, Point point, int position) throws SQLException {
        if(geometry == null || point == null){
            return null;
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
        throw new SQLException("First argument must be a LINESTRING");
    }

    /**
     * Adds a Point into a MultiPoint geometry.
     *
     * @param g
     * @param vertexPoint
     * @param position
     * @param factory
     * @return
     */
    private static Geometry insertVertexInMultipoint(MultiPoint g, Point vertexPoint, int position, GeometryFactory factory) throws SQLException {
        ArrayList<Point> geoms = new ArrayList<Point>();
        boolean added =false;
        for (int i = 0; i < g.getNumGeometries(); i++) {
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
     * @param lineString
     * @param vertexPoint
     * @param factory
     * @return
     * @throws SQLException
     */
    private static LineString insertVertexInLineString(LineString lineString, Point vertexPoint, GeometryFactory factory) throws SQLException {
        CoordinateSequence coordSeq = lineString.getCoordinateSequence();
        return factory.createLineString(addCoordinate(coordSeq, vertexPoint.getCoordinate(),coordSeq.size()+1 ));
    }

    /**
     * Inserts a vertex into a LineString with a given tolerance.
     *
     * @param lineString
     * @param vertexPoint
     * @param factory
     * @return
     * @throws SQLException
     */
    private static LineString insertVertexInLineString(LineString lineString, Point vertexPoint, int position, GeometryFactory factory) throws SQLException {
        return factory.createLineString(addCoordinate(lineString.getCoordinateSequence(), vertexPoint.getCoordinate(),position ));
    }
    /**
     * Expand the coordinates array and add a coordinate at the given position
     *
     * @param coorseq
     * @param position
     * @param point
     * @return
     */
    public static  Coordinate[] addCoordinate(CoordinateSequence coorseq,   Coordinate point, int position) {
        if(position<=coorseq.size() ){
            Coordinate coord = new CoordinateXY(point);
            if(coorseq.hasZ()&& coorseq.hasM()){
                coord = new CoordinateXYZM(point);
            }
            else if(coorseq.hasZ()){
                coord =new Coordinate(point.getX(), point.getY(), Double.isNaN(point.getZ())?0:point.getZ());
            }
            else if(coorseq.hasM()){
                coord = new CoordinateXYM(point.getX(), point.getY(), Double.isNaN(point.getM())?0:point.getM());
            }
            Coordinate[] coordinates =  coorseq.toCoordinateArray();
            coordinates[position]=coord;
            return coordinates;
        }else {
            Coordinate[] coordinates =  coorseq.toCoordinateArray();
            Coordinate[] destArray = new Coordinate[coordinates.length + 1];
            Coordinate coord = new CoordinateXY(point);
            if(coorseq.hasZ()&& coorseq.hasM()){
                coord = new CoordinateXYZM(point);
            }
            else if(coorseq.hasZ()){
                coord =new Coordinate(point.getX(), point.getY(), Double.isNaN(point.getZ())?0:point.getZ());
            }
            else if(coorseq.hasM()){
                coord = new CoordinateXYM(point);
            }
            CoordinateArrays.copyDeep(coordinates, 0, destArray, 0, coordinates.length);
            destArray[position-1] = coord;
            return destArray;
        }
    }
}
