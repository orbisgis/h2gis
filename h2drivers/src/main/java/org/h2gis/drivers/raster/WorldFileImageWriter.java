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

package org.h2gis.drivers.raster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import org.h2.api.GeoRaster;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialapi.ProgressVisitor;

/**
 * Save a raster to a world file image.
 * 
 * @author Erwan Bocher
 */
public class WorldFileImageWriter {

    
    
    public WorldFileImageWriter(){
        
    }
    
    public void write(Connection connection, String tableReference, File fileName, ProgressVisitor progress) {

    }
    

    public void write(GeoRaster rast, File fileName, boolean isH2) {
        if(isH2){
            
        }
        else{
           //TODO: Add POSTGIS support
            
        }

    }

    /**
     * Write the world file
     * @param rasterMetaData
     * @param file
     * @throws IOException 
     */
    public static void writeWorldFile(RasterUtils.RasterMetaData rasterMetaData, File file) throws IOException {
        final PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(file)));
        writer.println(rasterMetaData.scaleX);
        writer.println(rasterMetaData.skewX);
        writer.println(rasterMetaData.skewY);
        writer.println(rasterMetaData.scaleY);
        writer.println(rasterMetaData.ipX);
        writer.println(rasterMetaData.ipY);
        writer.close();
    }
}


