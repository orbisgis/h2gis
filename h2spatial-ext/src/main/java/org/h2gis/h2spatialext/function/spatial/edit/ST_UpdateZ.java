/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * This function replace the z component of (each vertex of) the
 * geometric parameter to the corresponding value given by a field.
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
     * Replace the z with same value.
     * NaN values are also updated.
     *
     * @param geometry
     * @param z
     * @return
     */
    public static Geometry updateZ(Geometry geometry, double z) {
        geometry.apply(new UpdateZCoordinateSequenceFilter(z, UpdateCondition.REPLACE_ALL));
        return geometry;
    }

    /**
     * Add the z value. NaN z can be replaced with a new z value
     *
     * @param geometry
     * @param z
     * @param updateCondition set if the NaN value must be updated or not
     * @return
     */
    public static Geometry updateZ(Geometry geometry, double z, int updateCondition) {
        geometry.apply(new UpdateZCoordinateSequenceFilter(z, updateCondition));
        return geometry;
    }

    /**
     * Replaces the z value to each vertex of the Geometry. A second z value could
     * be used to force NaN z with a new value.
     */
    public static class UpdateZCoordinateSequenceFilter implements CoordinateSequenceFilter {

        private boolean done = false;
        private final double z;
        private final double zNaN;

        public UpdateZCoordinateSequenceFilter(double z, double zNaN) {
            this.z = z;
            this.zNaN = zNaN;
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
            if (!Double.isNaN(zNaN)) {
                if (Double.isNaN(currentZ)) {
                    seq.setOrdinate(i, 2, zNaN);
                } else {
                    seq.setOrdinate(i, 2, z);
                }
            } else {
                seq.setOrdinate(i, 2, z);

            }
            if (i == seq.size()) {
                done = true;
            }
        }
    }   
}
