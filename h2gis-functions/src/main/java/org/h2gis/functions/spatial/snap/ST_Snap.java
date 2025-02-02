/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.snap;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlay.snap.GeometrySnapper;

import java.sql.SQLException;

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
    public static Geometry snap(Geometry geometryA, Geometry geometryB, double distance) throws SQLException {
        if (geometryA == null || geometryB == null) {
            return null;
        }
        if (geometryA.getSRID() == geometryB.getSRID()) {
            Geometry[] snapped = GeometrySnapper.snap(geometryA, geometryB, distance);
            return snapped[0];
        } else {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
    }
}
