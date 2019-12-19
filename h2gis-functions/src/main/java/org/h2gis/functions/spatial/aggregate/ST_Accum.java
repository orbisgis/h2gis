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

package org.h2gis.functions.spatial.aggregate;

import org.h2.api.Aggregate;
import org.h2.value.Value;
import org.h2gis.api.AbstractFunction;
import org.locationtech.jts.geom.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Construct an array of Geometries.
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher, CNRS
 */
public class ST_Accum extends AbstractFunction implements Aggregate {
    private List<Geometry> toUnite = new LinkedList<Geometry>();
    private int minDim = Integer.MAX_VALUE;
    private int maxDim = Integer.MIN_VALUE;
    private int srid =-1;

    public ST_Accum() {
        addProperty(PROP_REMARKS, "This aggregate function returns a GeometryCollection "
                + "from a column of mixed dimension Geometries.\n"
                + "If there is only POINTs in the column of Geometries, a MULTIPOINT is returned. \n"
                + "Same process with LINESTRINGs and POLYGONs.");
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

    /**
     * Add geometry into an array to accumulate
     * @param geom 
     */
    private void addGeometry(Geometry geom) {
        if (geom != null) {
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
    }

    @Override
    public void add(Object o) throws SQLException {
        if (o instanceof Geometry) {
            Geometry geom = (Geometry) o;
            if(srid ==-1){                
                srid=geom.getSRID();
            }
            if(srid==geom.getSRID()){
            addGeometry(geom);
            }
            else {
              throw new SQLException("Operation on mixed SRID geometries not supported");  
            }
        } else if (o != null) {
            throw new SQLException("ST_Accum accepts only Geometry values. Input: " +
                    o.getClass().getSimpleName());
        }
    }

    @Override
    public GeometryCollection getResult() throws SQLException {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), srid);        
        if(maxDim != minDim) {
            return factory.createGeometryCollection(toUnite.toArray(new Geometry[0]));
        } else {
            switch (maxDim) {
                case 0:
                    return factory.createMultiPoint(toUnite.toArray(new Point[0]));
                case 1:
                    return factory.createMultiLineString(toUnite.toArray(new LineString[0]));
                default:
                    return factory.createMultiPolygon(toUnite.toArray(new Polygon[0]));
            }
        }
    }
}
