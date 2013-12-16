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
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Compute the minimum rectangle to a geometry.
 * @author Erwan Bocher
 */
public class ST_MinimumRectangle extends DeterministicScalarFunction{

    
    public ST_MinimumRectangle(){
       addProperty(PROP_REMARKS, "Gets the minimum rectangular (Polygon) which encloses the input geometry.");
    }
    
    @Override
    public String getJavaStaticMethod() {
       return "computeMinimumRectangle";
    }
    
    /**
     * Gets the minimum rectangular {@link Polygon} which encloses the input geometry.
     * @param geometry
     * @return 
     */
    public static Geometry computeMinimumRectangle(Geometry geometry){     
        return new MinimumDiameter(geometry).getMinimumRectangle();
    }
    
}
