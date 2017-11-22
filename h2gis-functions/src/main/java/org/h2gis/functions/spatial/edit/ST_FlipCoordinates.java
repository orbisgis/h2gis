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

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Flip the X and Y coordinates of the geometry
 * 
 * @author Erwan Bocher
 */
public class ST_FlipCoordinates extends DeterministicScalarFunction {

    public ST_FlipCoordinates() {
        addProperty(PROP_REMARKS, "Returns a version of the given geometry with X and Y axis flipped. Useful for people who have built\n"
                + "latitude/longitude features and need to fix them.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "flipCoordinates";
    }

    public static Geometry flipCoordinates(Geometry geom) {
        if (geom != null) {
            geom.apply(new FlipCoordinateSequenceFilter());
            return geom;
        }
        return null;

    }

    /**
     * Returns a version of the given geometry with X and Y axis flipped.
     */
    public static class FlipCoordinateSequenceFilter implements CoordinateSequenceFilter {

        private boolean done = false;

        @Override
        public void filter(CoordinateSequence seq, int i) {
            double x = seq.getOrdinate(i, 0);
            double y = seq.getOrdinate(i, 1);
            seq.setOrdinate(i, 0, y);
            seq.setOrdinate(i, 1, x);
            if (i == seq.size()) {
                done = true;
            }
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean isGeometryChanged() {
            return true;
        }
    }
}
