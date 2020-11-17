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

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

/**
 * Forces a Geometry into 4D mode by returning a copy with
 *  z set to 0 (other z-measure are left untouched).
 *  M set to 0 (other z-measure are left untouched).
 *
 * @author Erwan Bocher
 */
public class ST_Force4D extends DeterministicScalarFunction {

    public ST_Force4D() {
        addProperty(PROP_REMARKS, "Forces the geometries into XYZM mode.\n "
                + "If a geometry has no Z  or M measure, then a 0 is tacked on.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "force4D";
    }

    /**
     * Converts a geometry to XYZm.
     * If a geometry has no Z  or M measure, then a 0 is tacked on.
     *
     * @param geom
     * @return
     */
    public static Geometry force4D(Geometry geom) {
        if (geom == null) {
            return null;
        }
        return GeometryCoordinateDimension.force(geom, 4);
    }
}
