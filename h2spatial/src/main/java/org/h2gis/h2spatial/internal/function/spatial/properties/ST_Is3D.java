/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatial.internal.function.spatial.properties;

import java.io.IOException;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.GeometryMetaData;

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
     * @param geom
     * @return
     * @throws IOException 
     */
    public static int is3D(byte[] geom) throws IOException {
         if (geom == null) {
            return 0;
        }
        return GeometryMetaData.getMetaDataFromWKB(geom).hasZ?1:0;              
    }

   
}
