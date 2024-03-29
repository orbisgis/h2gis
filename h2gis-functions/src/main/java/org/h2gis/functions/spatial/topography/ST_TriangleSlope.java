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

package org.h2gis.functions.spatial.topography;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.TriMarkers;
import org.locationtech.jts.geom.Geometry;

/**
* This function is used to compute the slope direction of a triangle.
* @author Erwan Bocher
*/
public class ST_TriangleSlope extends DeterministicScalarFunction{

    public ST_TriangleSlope(){
        addProperty(PROP_REMARKS, "Compute the slope of a triangle expressed in percents.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "computeSlope";
    }
    
    /**
     * @param geometry Triangle
     * @return slope of a triangle expressed in percents
     * @throws IllegalArgumentException Accept only triangles
     */
    public static Double computeSlope(Geometry geometry) throws IllegalArgumentException {
        if(geometry == null){
            return null;
        }
        return TriMarkers.getSlopeInPercent(TriMarkers.getNormalVector(TINFeatureFactory.createTriangle(geometry)), TINFeatureFactory.EPSILON);
    }

}
