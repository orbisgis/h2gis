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

package org.h2gis.functions.spatial.edit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * This function replace the z component of (each vertex of) the geometric
 * parameter to the corresponding value given by a field.
 *
 * @author Erwan Bocher
 */
public class ST_UpdateZ extends DeterministicScalarFunction {

    public ST_UpdateZ() {
        addProperty(PROP_REMARKS, "This function replace the z value of (each vertex of) the\n"
                + " geometric parameter to the corresponding value given by a field.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "updateZ";
    }

   

    /**
     * Replace the z with same value. NaN values are also updated.
     *
     * @param geometry
     * @param z
     * @return geometry
     */
    public static Geometry updateZ(Geometry geometry, double z) {
        if (geometry == null) {
            return null;
        }
        geometry.apply(new UpdateZCoordinateSequenceFilter(z));
        return geometry;
    }

    /**
     * Replaces the z value to each vertex of the Geometry.
     *
     */
    public static class UpdateZCoordinateSequenceFilter implements CoordinateSequenceFilter {
        
        private boolean done = false;
        private final double z;

        public UpdateZCoordinateSequenceFilter(double z) {
            this.z = z;
        }

        @Override
        public boolean isGeometryChanged() {
            return true;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public void filter(CoordinateSequence seq, int i) {            
            seq.setOrdinate(i, 2, z);
            if (i == seq.size()) {
                done = true;
            }
        }
    }
}
