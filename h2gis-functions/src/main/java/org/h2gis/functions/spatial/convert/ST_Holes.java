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

package org.h2gis.functions.spatial.convert;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Polygon;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * ST_Holes returns the given geometry or geometry collection's holes as a
 * GeometryCollection. Returns GEOMETRYCOLLECTION EMPTY for geometries of
 * dimension less than 2.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class ST_Holes extends DeterministicScalarFunction {

    public ST_Holes() {
        addProperty(PROP_REMARKS, "Returns the given geometry's holes as a " +
                "GeometryCollection.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Returns the given geometry's holes as a GeometryCollection.
     *
     * @param geom Geometry
     * @return The geometry's holes
     */
    public static GeometryCollection execute(Geometry geom) throws SQLException {
        if (geom != null) {
            if (geom.getDimension() >= 2) {
                ArrayList<Geometry> holes = new ArrayList<Geometry>();
                int size = geom.getNumGeometries();
                for (int i = 0; i < size; i++) {
                    Geometry subgeom = geom.getGeometryN(i);
                    if (subgeom instanceof Polygon) {
                        Polygon polygon = (Polygon) subgeom;
                        int sub_size = polygon.getNumInteriorRing();
                        for (int j = 0; j < sub_size; j++) {
                            holes.add(geom.getFactory().createPolygon(
                                    geom.getFactory().createLinearRing(
                                            polygon.getInteriorRingN(j).getCoordinates()), null));
                        }
                    }
                }
                return geom.getFactory().createGeometryCollection(
                        holes.toArray(new Geometry[0]));
            } else {
                return geom.getFactory().createGeometryCollection(null);
            }
        }
        return null;
    }
}
