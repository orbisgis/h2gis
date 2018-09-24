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

package org.h2gis.functions.spatial.properties;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Returns the last coordinate of a Geometry as a POINT, given that the
 * Geometry is a LINESTRING or a MULTILINESTRING containing only one
 * LINESTRING; Returns NULL for all other Geometries.
 *
 * @author Nicolas Fortin
 */
public class ST_EndPoint extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_EndPoint() {
        addProperty(PROP_REMARKS, "Returns the last coordinate of a Geometry as a " +
                "POINT, given that the Geometry is a LINESTRING or a " +
                "MULTILINESTRING containing only one LINESTRING. " +
                "Returns NULL for all other Geometries. ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getEndPoint";
    }

    /**
     * @param geometry Geometry
     * @return The last coordinate of a Geometry as a POINT, given that the
     * Geometry is a LINESTRING or a MULTILINESTRING containing only one
     * LINESTRING; Returns NULL for all other Geometries.
     */
    public static Geometry getEndPoint(Geometry geometry) {
        if(geometry== null){
            return null;
        }
        if (geometry instanceof MultiLineString) {
            if (geometry.getNumGeometries() == 1) {
                return ((LineString) geometry.getGeometryN(0)).getEndPoint();
            }
        } else if (geometry instanceof LineString) {
            return ((LineString) geometry).getEndPoint();
        }
        return null;
    }
}
