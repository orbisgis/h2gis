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

package org.h2gis.h2spatialext.function.spatial.raster;

import java.io.IOException;
import javax.media.jai.JAI;
import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_Band extends DeterministicScalarFunction{

    public ST_Band() {
        addProperty(PROP_REMARKS, "Returns one or more bands of an existing raster as a new raster.");
    }
    
    @Override
    public String getJavaStaticMethod() {
     return "bandSelect";
    }
    
    /**
     * Returns one or more inputBands of an existing raster as a new raster.  
     * @param rast
     * @param bandIndexes
     * @return
     * @throws IOException 
     */
    public static GeoRasterRenderedImage bandSelect(GeoRaster rast, int... bandIndexes) throws IOException{        
        if(rast==null){
            return null;
        }
        RasterUtils.RasterMetaData metadata = rast.getMetaData();

        RasterUtils.RasterBandMetaData[] inputBands = metadata.bands;
        
        if(bandIndexes.length>inputBands.length){
             throw new IllegalArgumentException("The number of band indexes doesn't match the number of bands in the raster.\n"
                     + "The raster contains "+ inputBands.length+ "band(s).");
        }

        int inputBandsNumb = inputBands.length;
        RasterUtils.RasterBandMetaData[] newBands = new RasterUtils.RasterBandMetaData[bandIndexes.length];
        int[] updateBandOrder = new int[bandIndexes.length];
        int i = 0;
        for (int bandIndex : bandIndexes) {
            if (bandIndex < 1) {
                throw new IllegalArgumentException("The band index must be greater or equal to 1.");
            } else if ((bandIndex - 1) > inputBandsNumb) {
                throw new IllegalArgumentException("This band index." + bandIndex+ " is out of the \n"
                     + "The raster contains "+ inputBands.length+ "band(s).");
            }else{
                updateBandOrder[i] = bandIndex - 1;
                newBands[i] = inputBands[bandIndex - 1];                
            }
            i++;
        }

        return GeoRasterRenderedImage.create(JAI.create("bandselect", rast, updateBandOrder),
                new RasterUtils.RasterMetaData(RasterUtils.LAST_WKB_VERSION, newBands.length, metadata.scaleX, metadata
                        .scaleY, metadata.ipX, metadata.ipY, metadata.skewX, metadata.skewY, metadata.srid, metadata
                        .width, metadata.height, newBands));
    }
}
