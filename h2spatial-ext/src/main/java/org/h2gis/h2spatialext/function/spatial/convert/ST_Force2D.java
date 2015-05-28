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
package org.h2gis.h2spatialext.function.spatial.convert;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Forces a Geometry into 2D mode by returning a copy with 
 * its z-coordinate set to {@link Double.NaN}.
 *
 * @author Erwan Bocher
 */
public class ST_Force2D extends DeterministicScalarFunction {

    public ST_Force2D() {
        addProperty(PROP_REMARKS, "Forces the geometries into a \"2-dimensional mode\" \n"
                + " so that all output representations will only have the X and Y coordinates.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "force2D";
    }

    /**
     * Converts a XYZ geometry to XY.
     *
     * @param geom
     * @return
     */
    public static Geometry force2D(Geometry geom) {
        if (geom == null) {
            return null;
        }
        Geometry outPut = (Geometry) geom.clone();
        outPut.apply(new CoordinateSequenceFilter() {
            private boolean done = false;

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
                seq.setOrdinate(i, 2, Double.NaN);
                if (i == seq.size()) {
                    done = true;
                }
            }
        });
        return outPut;
    }
}
