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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.OctagonalEnvelope;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Computes the octogonal envelope of a geometry.
 *
 * @see com.vividsolutions.jts.geom.OctagonalEnvelope
 * @author Erwan Bocher
 */
public class ST_OctogonalEnvelope extends DeterministicScalarFunction{

    private static final GeometryFactory GF = new GeometryFactory();
    
    public ST_OctogonalEnvelope(){
        addProperty(PROP_REMARKS, "Computes the octogonal envelope of a geometry");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "computeOctogonalEnvelope";
    }
    
    public static Geometry computeOctogonalEnvelope(Geometry geometry){
        return new OctagonalEnvelope(geometry).toGeometry(GF);
    }
}
