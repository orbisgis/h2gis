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

package org.h2gis.functions.spatial.convert;

import org.locationtech.jts.geom.*;
import org.h2gis.api.DeterministicScalarFunction;

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

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public ST_Holes() {
        addProperty(PROP_REMARKS, "Returns the given geometry's holes as a " +
                "GeometryCollection.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getHoles";
    }

    /**
     * Returns the given geometry's holes as a GeometryCollection.
     *
     * @param geom Geometry
     * @return The geometry's holes
     * @throws SQLException
     */
    public static GeometryCollection getHoles(Geometry geom) throws SQLException {
        if (geom != null) {
            if (geom.getDimension() >= 2) {
                ArrayList<Geometry> holes = new ArrayList<Geometry>();
                for (int i = 0; i < geom.getNumGeometries(); i++) {
                    Geometry subgeom = geom.getGeometryN(i);
                    if (subgeom instanceof Polygon) {
                        Polygon polygon = (Polygon) subgeom;
                        for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                            holes.add(GEOMETRY_FACTORY.createPolygon(
                                    GEOMETRY_FACTORY.createLinearRing(
                                            polygon.getInteriorRingN(j).getCoordinates()), null));
                        }
                    }
                }
                return GEOMETRY_FACTORY.createGeometryCollection(
                        holes.toArray(new Geometry[holes.size()]));
            } else {
                return GEOMETRY_FACTORY.createGeometryCollection(null);
            }
        }
        return null;
    }
}
