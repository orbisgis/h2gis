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
 *
 * @author ebocher
 */
public class ST_MultiplyZ extends DeterministicScalarFunction {

    public ST_MultiplyZ() {
        addProperty(PROP_REMARKS, "This function do a multiplication with the z value of (each vertex of) the\n"
                + " geometric parameter to the corresponding value given by a field.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "multiplyZ";
    }

    /**
     * Multiply the z values of the geometry by another double value. NaN values
     * are not updated.
     *
     * @param geometry
     * @param z
     * @return
     * @throws java.sql.SQLException
     */
    public static Geometry multiplyZ(Geometry geometry, double z) throws SQLException {
        if(geometry == null){
            return null;
        }
        geometry.apply(new MultiplyZCoordinateSequenceFilter(z));
        return geometry;
    }

    /**
     * Multiply the z value of each vertex of the Geometry by a double value.
     *
     */
    public static class MultiplyZCoordinateSequenceFilter implements CoordinateSequenceFilter {

        private boolean done = false;
        private final double z;

        public MultiplyZCoordinateSequenceFilter(double z) {
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
            Coordinate coord = seq.getCoordinate(i);
            double currentZ = coord.z;
            if (!Double.isNaN(currentZ)) {
                seq.setOrdinate(i, 2, currentZ * z);
            }
            if (i == seq.size()) {
                done = true;
            }
        }
    }
}
