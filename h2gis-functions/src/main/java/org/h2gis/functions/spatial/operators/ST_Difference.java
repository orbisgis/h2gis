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

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import java.sql.SQLException;

/**
 * Compute the difference between two Geometries.
 *
 * @author Nicolas Fortin
 */
public class ST_Difference extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_Difference() {
        addProperty(PROP_REMARKS, "Compute the difference between two Geometries.\n" +
                "If the gridSize argument is provided, the inputs geometries are snapped to a grid of the given size.");
        ;
    }

    @Override
    public String getJavaStaticMethod() {
        return "difference";
    }

    /**
     * @param a Geometry instance.
     * @param b Geometry instance
     * @return the difference between two geometries
     */
    public static Geometry difference(Geometry a, Geometry b) throws SQLException {
        if (a == null || b == null) {
            return null;
        }
        if (a.getSRID() == b.getSRID()) {
            return OverlayNGRobust.overlay(a, b, OverlayNG.DIFFERENCE);
        } else {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
    }

    /**
     * @param a Geometry instance.
     * @param b Geometry instance
     * @param gridSize size of a grid to snap the input geometries
     * @return the difference between two geometries
     */
    public static Geometry difference(Geometry a, Geometry b, double gridSize) throws SQLException {
        if (a == null || b == null) {
            return null;
        }
        if (a.getSRID() == b.getSRID()) {
            if (gridSize >= 0) {
                PrecisionModel pm = new PrecisionModel(1 / gridSize);
                return OverlayNG.overlay(a, b, OverlayNG.DIFFERENCE, pm);
            } else {
                return OverlayNGRobust.overlay(a, b, OverlayNG.DIFFERENCE);
            }
        } else {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
    }
}
