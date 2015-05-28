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
package org.h2gis.h2spatial.internal.function.spatial.aggregate;

import com.vividsolutions.jts.geom.*;
import org.h2.api.Aggregate;
import org.h2.value.Value;
import org.h2gis.h2spatialapi.AbstractFunction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Construct an array of Geometries.
 *
 * @author Nicolas Fortin
 */
public class ST_Accum extends AbstractFunction implements Aggregate {
    private List<Geometry> toUnite = new LinkedList<Geometry>();
    private int minDim = Integer.MAX_VALUE;
    private int maxDim = Integer.MIN_VALUE;

    public ST_Accum() {
        addProperty(PROP_REMARKS, "This aggregate function returns a GeometryCollection.");
    }

    @Override
    public void init(Connection connection) throws SQLException {
    }

    @Override
    public int getInternalType(int[] inputTypes) throws SQLException {
        if (inputTypes.length != 1) {
            throw new SQLException(ST_Accum.class.getSimpleName() + " expects 1 argument.");
        }
        if (inputTypes[0] != Value.GEOMETRY) {
            throw new SQLException(ST_Accum.class.getSimpleName() + " expects a Geometry argument");
        }
        return Value.GEOMETRY;
    }

    private void feedDim(Geometry geometry) {
        final int geomDim = geometry.getDimension();
        maxDim = Math.max(maxDim, geomDim);
        minDim = Math.min(minDim, geomDim);
    }

    private void addGeometry(Geometry geom) {
        if (geom instanceof GeometryCollection) {
            List<Geometry> toUnitTmp = new ArrayList<Geometry>(geom.getNumGeometries());
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                toUnitTmp.add(geom.getGeometryN(i));
                feedDim(geom.getGeometryN(i));
            }
            toUnite.addAll(toUnitTmp);
        } else {
            toUnite.add(geom);
            feedDim(geom);
        }
    }

    @Override
    public void add(Object o) throws SQLException {
        if (o instanceof Geometry) {
            Geometry geom = (Geometry) o;
            addGeometry(geom);
        } else if (o != null) {
            throw new SQLException("ST_Accum accepts only Geometry values. Input: " +
                    o.getClass().getSimpleName());
        }
    }

    @Override
    public GeometryCollection getResult() throws SQLException {
        GeometryFactory factory = new GeometryFactory();
        if(maxDim != minDim) {
            return factory.createGeometryCollection(toUnite.toArray(new Geometry[toUnite.size()]));
        } else {
            switch (maxDim) {
                case 0:
                    return factory.createMultiPoint(toUnite.toArray(new Point[toUnite.size()]));
                case 1:
                    return factory.createMultiLineString(toUnite.toArray(new LineString[toUnite.size()]));
                default:
                    return factory.createMultiPolygon(toUnite.toArray(new Polygon[toUnite.size()]));
            }
        }
    }
}
