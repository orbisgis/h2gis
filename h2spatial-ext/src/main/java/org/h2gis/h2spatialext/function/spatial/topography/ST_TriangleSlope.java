/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.topography;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DTriangle;

/**
* This function is used to computed the slope direction of a triangle
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
     * Compute the slope of a triangle expressed in percents
     * @param geometry
     * @return
     * @throws DelaunayError 
     */
    public static double computeSlope(Geometry geometry) throws DelaunayError {
        DTriangle triangle = TINFeatureFactory.createDTriangle(geometry);
        return triangle.getSlopeInPercent();
    }

}
