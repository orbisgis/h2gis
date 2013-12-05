/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.geom.*;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * ST_MakeLine constructs a LINESTRING from two POINT geometries.
 *
 * @author Adam Gouge
 */
public class ST_MakeLine extends DeterministicScalarFunction {

    public ST_MakeLine() {
        addProperty(PROP_REMARKS, "Constructs a LINESTRING from two POINT geometries.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createLine";
    }

    /**
     * Constructs a LINESTRING from the given POINTs
     *
     * @param optionalPoints Points
     * @return The LINESTRING constructed from the given POINTs
     */
    public static LineString createLine(Puntal pointA, Puntal pointB, Puntal... optionalPoints) throws SQLException {
        List<Coordinate> coordinateList = new LinkedList<Coordinate>();
        addCoordinatesToList(pointA, coordinateList);
        addCoordinatesToList(pointB, coordinateList);
        for (int i = 0; i < optionalPoints.length; i++) {
            addCoordinatesToList(optionalPoints[i], coordinateList);
        }
        return ((Geometry) pointA).getFactory().createLineString(
                coordinateList.toArray(new Coordinate[optionalPoints.length]));
    }

    private static void addCoordinatesToList(Puntal puntal, List<Coordinate> list) {
        if (puntal instanceof Point) {
            list.add(((Point) puntal).getCoordinate());
        }
        if (puntal instanceof MultiPoint) {
            list.addAll(Arrays.asList(((MultiPoint) puntal).getCoordinates()));
        }
    }

    /**
     * Constructs a LINESTRING from the given POINTs
     *
     * @param points Points
     * @return The LINESTRING constructed from the given POINTs
     */
    public static LineString createLine(GeometryCollection points) throws SQLException {
        final int size = points.getNumGeometries();
        if (size < 2) {
            throw new SQLException("At least two points are required to make a line.");
        }
        List<Coordinate> coordinateList = new LinkedList<Coordinate>();
        for (int i = 0; i < size; i++) {
            coordinateList.add(points.getGeometryN(i).getCoordinate());
        }
        return points.getGeometryN(0).getFactory().createLineString(
                coordinateList.toArray(new Coordinate[size]));
    }
}
