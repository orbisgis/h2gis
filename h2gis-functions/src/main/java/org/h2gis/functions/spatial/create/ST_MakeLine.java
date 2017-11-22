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

package org.h2gis.functions.spatial.create;

import org.locationtech.jts.geom.*;
import org.h2gis.api.DeterministicScalarFunction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * ST_MakeLine constructs a LINESTRING from POINT and MULTIPOINT geometries.
 *
 * @author Adam Gouge
 */
public class ST_MakeLine extends DeterministicScalarFunction {

    public static final int REQUIRED_NUMBER_OF_POINTS = 2;

    public ST_MakeLine() {
        addProperty(PROP_REMARKS, "Constructs a LINESTRING from two POINT geometries.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createLine";
    }

    /**
     * Constructs a LINESTRING from the given POINTs or MULTIPOINTs
     *
     * @param pointA The first POINT or MULTIPOINT
     * @param optionalPoints Optional POINTs or MULTIPOINTs
     * @return The LINESTRING constructed from the given POINTs or MULTIPOINTs
     * @throws SQLException
     */
    public static LineString createLine(Geometry pointA, Geometry... optionalPoints) throws SQLException {
        if( pointA == null || optionalPoints.length > 0 && optionalPoints[0] == null) {
            return null;
        }
        if (pointA.getNumGeometries() == 1 && !atLeastTwoPoints(optionalPoints, countPoints(pointA))) {
            throw new SQLException("At least two points are required to make a line.");
        }
        List<Coordinate> coordinateList = new LinkedList<Coordinate>();
        addCoordinatesToList(pointA, coordinateList);
        for (Geometry optionalPoint : optionalPoints) {
            addCoordinatesToList(optionalPoint, coordinateList);
        }
        return ((Geometry) pointA).getFactory().createLineString(
                coordinateList.toArray(new Coordinate[optionalPoints.length]));
    }

    /**
     * Constructs a LINESTRING from the given collection of POINTs and/or
     * MULTIPOINTs
     *
     * @param points Points
     * @return The LINESTRING constructed from the given collection of POINTs
     * and/or MULTIPOINTs
     * @throws SQLException
     */
    public static LineString createLine(GeometryCollection points) throws SQLException {
        if(points == null) {
            return null;
        }
        final int size = points.getNumGeometries();
        if (!atLeastTwoPoints(points)) {
            throw new SQLException("At least two points are required to make a line.");
        }
        List<Coordinate> coordinateList = new LinkedList<Coordinate>();
        for (int i = 0; i < size; i++) {
            coordinateList.addAll(Arrays.asList(points.getGeometryN(i).getCoordinates()));
        }
        return points.getGeometryN(0).getFactory().createLineString(
                coordinateList.toArray(new Coordinate[size]));
    }

    private static void addCoordinatesToList(Geometry puntal, List<Coordinate> list) throws SQLException {
        if (puntal instanceof Point) {
            list.add(puntal.getCoordinate());
        } else if (puntal instanceof MultiPoint) {
            list.addAll(Arrays.asList(puntal.getCoordinates()));
        } else {
            throw new SQLException("Only Points and MultiPoints are accepted.");
        }
    }

    private static boolean atLeastTwoPoints(GeometryCollection points) throws SQLException {
        return atLeastTwoPoints(points, 0);
    }

    /**
     * Returns true as soon as we know the collection contains at least two
     * points. Start counting from the initial number of points.
     *
     * @param points                Collection of points
     * @param initialNumberOfPoints The initial number of points
     * @return True as soon as we know the collection contains at least two
     *         points.
     */
    private static boolean atLeastTwoPoints(Geometry[] points,
                                            int initialNumberOfPoints) throws SQLException {
        if (points.length < 1) {
            throw new SQLException("The geometry collection must not be empty");
        }
        return atLeastTwoPoints(points[0].getFactory().createGeometryCollection(points),
                initialNumberOfPoints);
    }

    /**
     * Returns true as soon as we know the collection contains at least two
     * points. Start counting from the initial number of points.
     *
     * @param points                Collection of points
     * @param initialNumberOfPoints The initial number of points
     * @return True as soon as we know the collection contains at least two
     *         points.
     */
    private static boolean atLeastTwoPoints(GeometryCollection points,
                                            int initialNumberOfPoints) throws SQLException {
        int numberOfPoints = initialNumberOfPoints;
        for (int i = 0; i < points.getNumGeometries(); i++) {
            Geometry p = points.getGeometryN(i);
            if (numberOfPoints >= REQUIRED_NUMBER_OF_POINTS) {
                return true;
            }
            numberOfPoints = numberOfPoints + countPoints(p);
        }
        return numberOfPoints >= REQUIRED_NUMBER_OF_POINTS;
    }

    private static int countPoints(Geometry p) throws SQLException {
        if (p instanceof Point) {
            return 1;
        } else if (p instanceof MultiPoint) {
            return p.getNumPoints();
        } else {
            throw new SQLException("Only Points and MultiPoints are accepted.");
        }
    }
}
