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

package org.h2gis.functions.spatial.snap;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlay.snap.GeometrySnapper;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Snaps two geometries together with a given tolerance
 *
 * @author Erwan Bocher
 */
public class ST_Snap extends DeterministicScalarFunction {

    public ST_Snap() {
        addProperty(PROP_REMARKS, "Snaps two geometries together with a given tolerance");
    }

    @Override
    public String getJavaStaticMethod() {
        return "snap";
    }

    /**
     * Snaps two geometries together with a given tolerance
     *
     * @param geometryA a geometry to snap
     * @param geometryB a geometry to snap
     * @param distance the tolerance to use
     * @return the snapped geometries
     */
    public static Geometry snap(Geometry geometryA, Geometry geometryB, double distance) {
        if(geometryA == null||geometryB == null){
            return null;
        }
        Geometry[] snapped = GeometrySnapper.snap(geometryA, geometryB, distance);
        return snapped[0];
    }
}
