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

package org.h2gis.functions.spatial.distance;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.h2gis.api.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_ProjectPoint extends DeterministicScalarFunction{

    
    public ST_ProjectPoint(){
        addProperty(PROP_REMARKS, "Projet a point along a linestring. If the point projected is out of line "
                        + "the first or last point on the line will be returned otherwise"
                        + " the input point.");
    }
    @Override
    public String getJavaStaticMethod() {
        return "projectPoint";
    }
    
    
    /**
     * Project a point on a linestring or multilinestring
     * @param point
     * @param geometry
     * @return 
     */
    public static Point projectPoint(Geometry point, Geometry geometry) {
        if (point == null || geometry==null) {
            return null;
        }
        if (point.getDimension()==0 && geometry.getDimension() == 1) {
            LengthIndexedLine ll = new LengthIndexedLine(geometry);
            double index = ll.project(point.getCoordinate());
            Point result = geometry.getFactory().createPoint(ll.extractPoint(index));
            return result;
        } else {
            return null;
        }
    }
    
}
