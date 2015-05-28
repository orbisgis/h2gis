/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
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
     * @throws java.sql.SQLException
     */
    public static Geometry addZ(Geometry geometry, double z) throws SQLException {
        if(geometry == null){
            return null;
        }
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
