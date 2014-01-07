/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
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
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.GeometryEdit;

/**
 * Remove holes in a polygon or multipolygon
 * @author Erwan Bocher
 */
public class ST_RemoveHoles extends DeterministicScalarFunction{

    public ST_RemoveHoles(){
        addProperty(PROP_REMARKS, "Remove all holes in a polygon or a multipolygon. "
                + "\n If the geometry doesn't contain any hole return the input geometry.");
    }
    @Override
    public String getJavaStaticMethod() {
        return "removeHoles";
    }
    
    /**
     * Remove hole in a geometry. 
     * If geometry doesn't contain any holes return
     * the same geometry
     * @param geometry
     * @return 
     */
    public static Geometry removeHoles(Geometry geometry){
        return GeometryEdit.removeHoles(geometry);
    }
    
}
