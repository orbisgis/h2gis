/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 * <p/>
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 * <p/>
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.h2spatialext.function.spatial.raster;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.h2.api.GeoRaster;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.h2spatialext.jai.VectorizeDescriptor;
import sun.security.util.DerEncoder;

/**
 * Extract polygons from a raster
 * 
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class ST_DumpAsPolygons extends DeterministicScalarFunction{

    
    public ST_DumpAsPolygons(){
        addProperty(PROP_REMARKS, "Returns a set of geometry and corresdponding pixel value as rows, from a given raster band. \n"
                + "If no band indice is specified, the default is the first one.");
        VectorizeDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "vectorizePolygons";
    }
    
    
    /**
     * Vectorize the first band of the input raster.
     * nodata values are excluded to the vectorization process.
     * @param geoRaster
     * @return 
     * @throws java.io.IOException 
     * @throws java.sql.SQLException 
     */
    public static ResultSet vectorizePolygons(GeoRaster geoRaster) throws IOException, SQLException{        
       return  vectorizePolygons( geoRaster, 1, true);
        
    }
    
    /**
     * Vectorize a specific band of the input raster
     * 
     * @param geoRaster the input georaster
     * @param bandIndice the band indice
     * @param excludeNodata true to exclude nodata value
     * @return 
     * @throws java.io.IOException 
     * @throws java.sql.SQLException 
     */
    public static ResultSet vectorizePolygons(GeoRaster geoRaster, int bandIndice, boolean excludeNodata) throws IOException, SQLException{        
        if(geoRaster==null){
            return null;
        }
        if(bandIndice<1){
            throw new IllegalArgumentException("The band indice must be greater or equal to 1.");
        }
        VectorizeRowSet vectorizeRowSet  = new VectorizeRowSet(geoRaster, bandIndice-1, excludeNodata);        
        return vectorizeRowSet.getResultSet();       
        
    }
    
}
