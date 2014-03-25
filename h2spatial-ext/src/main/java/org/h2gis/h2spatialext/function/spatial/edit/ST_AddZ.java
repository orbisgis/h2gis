/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;

/**
 * This function add a z value to the z component of (each vertex of) the
 * geometric parameter to the corresponding value given by a field.
 *
 * @author Erwan Bocher
 */
public class ST_AddZ extends DeterministicScalarFunction {

    public ST_AddZ() {
        addProperty(PROP_REMARKS, "This function do a sum with the z value of (each vertex of) the\n"
                + " geometric parameter to the corresponding value given by a field.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "addZ";
    }

    /**
     * Add a z with to the existing value (do the sum). NaN values are not
     * updated.
     *
     * @param geometry
     * @param z
     * @return
     */
    public static Geometry addZ(Geometry geometry, double z) throws SQLException {
        geometry.apply(new AddZCoordinateSequenceFilter(z));
        return geometry;
    }

    /**
     * Add a z value to each vertex of the Geometry.
     *
     */
    public static class AddZCoordinateSequenceFilter implements CoordinateSequenceFilter {

        private boolean done = false;
        private final double z;

        public AddZCoordinateSequenceFilter(double z) {
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
                seq.setOrdinate(i, 2, currentZ + z);
            }
            if (i == seq.size()) {
                done = true;
            }
        }
    }
}
