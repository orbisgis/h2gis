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

package org.h2gis.h2spatialext.function.spatial.affine_transformations;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Compute a perspective projection
 * 
 * @author Erwan Bocher
 */
public class ST_PerspectiveProjection extends DeterministicScalarFunction{

    @Override
    public String getJavaStaticMethod() {
        return "perspectiveProjection";
    }
    
    /**
     * 
     * @param geom
     * @param pointEye
     * @return 
     */    
    public static Geometry perspectiveProjection(Geometry geom, Geometry pointEye) throws IllegalArgumentException{
        if(geom !=null || pointEye !=null){
            if(pointEye instanceof Point){
            final Geometry clone = (Geometry) geom.clone();
            clone.apply(new PerspectiveProjectionFilter(pointEye));
            return clone;
            }
            else{
                throw new IllegalArgumentException();
            }
        }
        return null;
    }
    
    public static class PerspectiveProjectionFilter implements Cloneable, CoordinateSequenceFilter{
        private final Coordinate eye;

        public PerspectiveProjectionFilter(Geometry pointEye){
             eye = pointEye.getCoordinate();   
             if(Double.isNaN(eye.z)){
                 throw new IllegalArgumentException("Must have a z");
             }
        }
        
        @Override
        public void filter(CoordinateSequence seq, int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isDone() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isGeometryChanged() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
}
