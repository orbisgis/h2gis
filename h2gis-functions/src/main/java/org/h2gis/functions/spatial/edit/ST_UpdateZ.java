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
                + " geometric parameter to the corresponding value given by a field.\n"
                + "The first argument is used to replace all existing z values.\n"
                + "The second argument is a int value. \n Set 1 to replace all z values.\n"
                + "Set 2 to replace all z values excepted the NaN values.\n"
                + "Set 3 to replace only the NaN z values.");
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
     * @return
     * @throws java.sql.SQLException
     */
    public static Geometry updateZ(Geometry geometry, double z) throws SQLException {
        return updateZ(geometry, z, 1);
    }

    /**
     * Replace the z value depending on the condition.
     *
     * @param geometry
     * @param z
     * @param updateCondition set if the NaN value must be updated or not
     * @return
     * @throws java.sql.SQLException
     */
    public static Geometry updateZ(Geometry geometry, double z, int updateCondition) throws SQLException {
        if(geometry == null){
            return null;
        }
        if (updateCondition == 1 || updateCondition == 2 || updateCondition == 3) {
            geometry.apply(new UpdateZCoordinateSequenceFilter(z, updateCondition));
            return geometry;
        } else {
            throw new SQLException("Available values are 1, 2 or 3.\n"
                    + "Please read the description of the function to use it.");
        }
    }

    /**
     * Replaces the z value to each vertex of the Geometry. 
     * If the condition is equal to 1, replace all z. 
     * If the consition is equal to 2 replace only not NaN z. 
     * If the condition is equal to 3 replace only NaN z.
     *
     */
    public static class UpdateZCoordinateSequenceFilter implements CoordinateSequenceFilter {

        private boolean done = false;
        private final double z;
        private final int condition;

        public UpdateZCoordinateSequenceFilter(double z, int condition) {
            this.z = z;
            this.condition = condition;
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
            if (condition == 1) {
                seq.setOrdinate(i, 2, z);
            } else if (condition == 2) {
                Coordinate coord = seq.getCoordinate(i);
                double currentZ = coord.z;
                if (!Double.isNaN(currentZ)) {
                    seq.setOrdinate(i, 2, z);
                }
            } else if (condition == 3) {
                Coordinate coord = seq.getCoordinate(i);
                double currentZ = coord.z;
                if (Double.isNaN(currentZ)) {
                    seq.setOrdinate(i, 2, z);
                } 
            } else {
                done = true;
            }
            if (i == seq.size()) {
                done = true;
            }
        }
    }
}
