/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.properties;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

/**
 * ST_Perimeter returns the perimeter of a polygon or a multipolygon.
 * 
 * @author Erwan Bocher
 */
public class ST_Perimeter extends DeterministicScalarFunction{
   

    public ST_Perimeter() {
        addProperty(PROP_REMARKS, "Returns the length measurement of the boundary of a Polygon or a MultiPolygon. \n"
                + "Distance units are those of the geometry spatial reference system.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "perimeter";
    }
    
    /**
     * Compute the perimeter of a polygon or a multipolygon.
     * @param geometry {@link Geometry}
     * @return perimeter
     */
    public static Double perimeter(Geometry geometry){        
        if(geometry==null){
            return null;
        }
        if(geometry.getDimension()<2){
            return 0d;
        }
        return computePerimeter(geometry);
    }
    
    /**
     * Compute the perimeter
     * @param geometry {@link Geometry}
     * @return perimeter
     */
    private static double computePerimeter(Geometry geometry) {
        double sum = 0;
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry subGeom = geometry.getGeometryN(i);
            if (subGeom instanceof Polygon) {
                sum += ((Polygon) subGeom).getExteriorRing().getLength();
            } 
        }
        return sum;
    }    
}
