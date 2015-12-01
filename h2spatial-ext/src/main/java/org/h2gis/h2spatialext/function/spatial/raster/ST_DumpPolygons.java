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
import java.sql.Connection;
import java.sql.ResultSet;
import org.h2.api.GeoRaster;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.h2spatialext.jai.VectorizeDescriptor;

/**
 * 
 * @author Erwan Bocher
 */
public class ST_DumpPolygons extends AbstractFunction implements ScalarFunction{

    
    public ST_DumpPolygons(){
        addProperty(PROP_REMARKS, "");
        VectorizeDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "vectorizePolygons";
    }
    
    
    /**
     * 
     * @param connection
     * @param geoRaster
     * @return 
     */
    public static ResultSet vectorizePolygons(Connection connection, GeoRaster geoRaster) throws IOException{        
       return  vectorizePolygons(connection, geoRaster, 1, true);
        
    }
    
    /**
     * 
     * @param connection
     * @param geoRaster
     * @param bandIndice
     * @param excludeNodata
     * @return 
     * @throws java.io.IOException 
     */
    public static ResultSet vectorizePolygons(Connection connection, GeoRaster geoRaster, int bandIndice, boolean excludeNodata) throws IOException{        
        if(geoRaster==null){
            return null;
        }        
        VectorizeRowSet vectorizeRowSet  = new VectorizeRowSet(connection,
                ST_Band.bandSelect(geoRaster, new int[]{bandIndice}), excludeNodata);        
        return vectorizeRowSet.getResultSet();       
        
    }
    
}
