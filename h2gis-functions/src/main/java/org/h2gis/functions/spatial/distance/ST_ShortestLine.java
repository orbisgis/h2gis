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

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.distance.DistanceOp;

/**
 * Returns the 2-dimensional shortest line between two geometries.
 * @author Erwan Bocher
 */
public class ST_ShortestLine extends  DeterministicScalarFunction{

    
    public ST_ShortestLine(){
        addProperty(PROP_REMARKS, "Returns the 2-dimensional shortest line between two geometries. \nThe function will only return the first shortest line if more than one, that the function finds.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "shortestLine";
    }
 
    /**
     * Compute the shortest line between two geometries.
     * @param geomA
     * @param geomB
     * @return 
     */
    public static LineString shortestLine(Geometry geomA, Geometry geomB){
        if (geomA == null || geomB == null) {
            return null;
        }
        if (geomA.isEmpty() || geomB.isEmpty()) {
            return null;
        }        
        Coordinate[] pts = DistanceOp.nearestPoints(geomA, geomB);
        return geomA.getFactory().createLineString(pts);
    }
}
