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

package org.h2gis.h2spatialext.function.spatial.convert;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import org.h2.api.AggregateTypeFunction;
import org.h2gis.h2spatialapi.AbstractFunction;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * ST_PointsToLine returns a LineString from a column of Points or MultiPoints.
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_PointsToLine extends AbstractFunction implements AggregateTypeFunction {

    private static GeometryFactory GF;
    private List<Coordinate> coords;

    public ST_PointsToLine() {
        addProperty(PROP_REMARKS, "Returns a LineString from a column of Points or MultiPoints.");
    }

    @Override
    public void init(Connection conn) throws SQLException {
        GF = new GeometryFactory();
        coords = new LinkedList<Coordinate>();
    }

    @Override
    public ColumnType getType(int[] inputTypes, String[] inputTypesName) throws SQLException {
        if(inputTypes.length!=1) {
            throw new SQLException(ST_PointsToLine.class.getSimpleName()+" expect 1 argument.");
        }
        if(inputTypes[0]!=Types.OTHER && inputTypes[0]!=Types.JAVA_OBJECT && !inputTypesName[0].equalsIgnoreCase("geometry")) {
            throw new SQLException(ST_PointsToLine.class.getSimpleName()+" expect a geometry argument");
        }
        return new ColumnType(Types.JAVA_OBJECT, "GEOMETRY");
    }

    @Override
    public void add(Object o) throws SQLException {
        if (o != null) {
            if (o instanceof Geometry) {
                Geometry geom = (Geometry) o;
                if (geom instanceof Point) {
                    coords.add(geom.getCoordinate());
                } else if (geom instanceof MultiPoint) {
                    coords.addAll(Arrays.asList(geom.getCoordinates()));
                } else {
                    throw new SQLException("ST_PointsToLine accepts only (Multi)Points as input.");
                }
            } else {
                throw new SQLException();
            }
        }
    }

    @Override
    public LineString getResult() throws SQLException {
        if (coords.size() > 2) {
            return GF.createLineString(coords.toArray(new Coordinate[coords.size()]));
        }
        return null;
    }
}
