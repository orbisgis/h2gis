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

package org.h2gis.functions.spatial.generalize;

import java.sql.SQLException;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.VWSimplifier;

/**
 * Returns a simplified version of the given geometry using the Douglas-Peuker
 * algorithm.
 *
 * @author Maël PHILIPPE
 */
public class ST_SimplifyVW extends DeterministicScalarFunction {

    public ST_SimplifyVW() {
        addProperty(PROP_REMARKS,
                "Returns a simplified version of the given geometry using the Douglas-Peuker algorithm.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "simplifyvw";
    }

    /**
     * Simplifies a geometry using a given tolerance.
     * 
     * @param geom              geometry to simplify
     * @param distanceTolerance the tolerance to use
     * @return a simplified version of the geometry
     */
    public static Geometry simplifyvw(Geometry geom, double distanceTolerance) throws SQLException {
        if (geom == null) {
            return null;
        }

        if (distanceTolerance < 0.0) {
            throw new IllegalArgumentException("Area must be non-negative");
        } else {
            VWSimplifier simp = new VWSimplifier(geom);
            simp.setDistanceTolerance(Math.sqrt(distanceTolerance));
            return simp.getResultGeometry();
        }

    }

}
