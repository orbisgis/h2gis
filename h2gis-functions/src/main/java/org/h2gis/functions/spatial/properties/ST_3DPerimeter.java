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

package org.h2gis.functions.spatial.properties;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.h2gis.api.DeterministicScalarFunction;

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
     * @param geometry
     * @return 
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
     * @param geometry
     * @return 
     */
    private static double compute3DPerimeter(Geometry geometry) {
        double sum = 0;
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry subGeom = geometry.getGeometryN(i);
            if (subGeom instanceof Polygon) {
                sum += ST_3DLength.length3D(((Polygon) subGeom).getExteriorRing());
            } 
        }
        return sum;
    }    
}
