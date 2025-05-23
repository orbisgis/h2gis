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
 * ST_3DPerimeter returns the 3D perimeter of a polygon or a multipolygon.
 * In the case of a 2D geometry, ST_3DPerimeter returns the same value as ST_Perimeter.
 * 
 * @author Erwan Bocher
 */
public class ST_3DPerimeter extends DeterministicScalarFunction{
   

    public ST_3DPerimeter() {
        addProperty(PROP_REMARKS, "Returns the 3D length measurement of the boundary of a Polygon or a MultiPolygon. \n"
                + "Note : For 2D geometries, returns the 2D length.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "st3Dperimeter";
    }
    
    /**
     * Compute the 3D perimeter of a polygon or a multipolygon.
     * @param geometry {@link Geometry}
     * @return the 3D perimeter
     */
    public static Double st3Dperimeter(Geometry geometry){        
        if(geometry==null){
            return null;
        }
        if(geometry.getDimension()<2){
            return 0d;
        }
        return compute3DPerimeter(geometry);
    }
    
    /**
     * Compute the 3D perimeter
     * @param geometry {@link Geometry}
     * @return 3D perimeter
     */
    private static double compute3DPerimeter(Geometry geometry) {
        double sum = 0;
        int size  = geometry.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry subGeom = geometry.getGeometryN(i);
            if (subGeom instanceof Polygon) {
                sum += ST_3DLength.length3D(((Polygon) subGeom).getExteriorRing());
            } 
        }
        return sum;
    }    
}
