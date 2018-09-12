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

package org.h2gis.functions.spatial.geometry;

import org.h2gis.api.DeterministicScalarFunction;
import static org.h2gis.api.Function.PROP_REMARKS;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;

/**
 * Dummy spatial function for test
 * @author Erwan Bocher
 */
public class DummySpatialFunction extends DeterministicScalarFunction {

    public static final String REMARKS = "Dummy spatial function description";

    public DummySpatialFunction() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "returnGeom";
    }

    public static Geometry returnGeom(Geometry geom) {
        return geom;
    }
    
    /**
     * If true all z are replaced by Double.NaN
     * If false only the first z
     * @param geom
     * @param setZtoNaN
     * @return 
     */
    public static Geometry returnGeom(Geometry geom, boolean setZtoNaN) {
        UpdateZCoordinateSequenceFilter updateZCoordinateSequenceFilter = new UpdateZCoordinateSequenceFilter(Double.NaN, setZtoNaN);
        geom.apply(updateZCoordinateSequenceFilter);

        return geom;
    }
    
    
     /**
     * Replaces the z value to each vertex of the Geometry.
     *
     */
    public static class UpdateZCoordinateSequenceFilter implements CoordinateSequenceFilter {
        
        private boolean done = false;
        private final double z;
        private final boolean zCondition;

        public UpdateZCoordinateSequenceFilter(double z, boolean zCondition) {
            this.z = z;
            this.zCondition = zCondition;
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
            if(!zCondition){
                done =true;
            }
            if (i == seq.size()) {
                done = true;
            }
        }
    }
}
