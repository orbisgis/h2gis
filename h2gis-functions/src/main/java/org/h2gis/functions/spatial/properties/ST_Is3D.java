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
import org.h2gis.utilities.GeometryMetaData;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;

/**
 * Returns 1 if a geometry has a z-coordinate, otherwise 0.
 * 
 * Implements the SQL/MM Part 3: Spatial 5.1.3
 * 
 * @author Erwan Bocher
 */
public class ST_Is3D extends DeterministicScalarFunction{

    
    public ST_Is3D(){
        addProperty(PROP_REMARKS, "Returns 1 if a geometry has a z-coordinate, otherwise 0.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "is3D";
    }
    
    /**
     * Returns 1 if a geometry has a z-coordinate, otherwise 0.
     * @param geom {@link Geometry}
     * @return 1 if it's a 3D geom, 0 otherwise
     */
    public static int is3D(Geometry geom) throws IOException {
         if (geom == null) {
            return 0;
        }
        return GeometryMetaData.getMetaData(geom).hasZ?1:0;              
    }

   
}
