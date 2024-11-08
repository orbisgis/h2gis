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
 * Snaps a geometry to itself with a given tolerance
 * Allows optionally cleaning the result to ensure it is topologically valid
 *
 * @author Erwan Bocher
 */
public class ST_SnapToSelf extends DeterministicScalarFunction {

    public ST_SnapToSelf() {
        addProperty(PROP_REMARKS, "Snaps a geometry to itself with a given tolerance.\n" +
                "Allows optionally cleaning the result to ensure it is topologically valid. true by default");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }


    /**
     * Snaps a geometry to itself with a given tolerance
     *
     * @param geometryA a geometry to snap
     * @param distance the tolerance to use
     * @return the snapped geometries
     */
    public static Geometry execute(Geometry geometryA, double distance ) throws SQLException {
        return execute(geometryA, distance, true);
    }
    /**
     * Snaps a geometry to itself with a given tolerance
     *
     * @param geometryA a geometry to snap
     * @param distance the tolerance to use
     * @param clean true to clean the geometry
     * @return the snapped geometries
     */
    public static Geometry execute(Geometry geometryA, double distance, boolean clean ) throws SQLException {
        if (geometryA == null ) {
            return null;
        }
        return GeometrySnapper.snapToSelf(geometryA, distance, clean);
    }
}
