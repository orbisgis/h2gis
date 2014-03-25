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

package org.h2gis.h2spatialext.function.spatial.convert;

import com.vividsolutions.jts.geom.*;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
