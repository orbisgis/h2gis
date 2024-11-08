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

package org.h2gis.functions.spatial.operators;

import java.sql.SQLException;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

/**
 * Compute the union of two or more Geometries.
 *
 * @author Nicolas Fortin
 */
public class ST_Union extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_Union() {
        addProperty(PROP_REMARKS, "Compute the union of two or more Geometries.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "union";
    }

    /**
     * @param a Geometry instance.
     * @param b Geometry instance
     * @return union of Geometries a and b
     */
    public static Geometry union(Geometry a, Geometry b) throws SQLException {
        if (a == null || b == null) {
            return null;
        }
        if (a.isEmpty()) {
            return a;
        }
        if (b.isEmpty()) {
            return OverlayNGRobust.union(a);
        }
        if (a.getSRID() != b.getSRID()) {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return OverlayNGRobust.overlay(a, b, OverlayNG.UNION);
    }


    /**
     * @param geomList Geometry list
     * @return union of all Geometries in geomList
     */
    public static Geometry union(Geometry geomList) {
        if (geomList == null) {
            return null;
        }
        if (geomList.isEmpty()) {
            return geomList;
        }
        return OverlayNGRobust.union(geomList);
    }
}
