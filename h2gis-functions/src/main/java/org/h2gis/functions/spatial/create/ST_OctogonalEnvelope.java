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

package org.h2gis.functions.spatial.create;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.OctagonalEnvelope;

/**
 * Computes the octogonal envelope of a geometry.
 *
 * @see org.locationtech.jts.geom.OctagonalEnvelope
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
        if(geometry == null){
            return null;
        }
        return new OctagonalEnvelope(geometry).toGeometry(GF);
    }
}
