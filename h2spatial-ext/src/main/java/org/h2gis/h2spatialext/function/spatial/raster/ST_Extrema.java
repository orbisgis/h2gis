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

import java.awt.*;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ConstantDescriptor;

import org.h2.api.GeoRaster;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.jai.UnaryFunction;
import org.h2gis.h2spatialext.jai.UnaryFunctionDescriptor;

/**
 * Return the min and max value for one band of the input raster.
 *
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class ST_Extrema extends DeterministicScalarFunction {

    
    public ST_Extrema(){
        addProperty(PROP_REMARKS, "Compute the min and max value of a geoRaster.\n"
                + "Note : "
                + "- the geoRaster must contains only one band,\n"
                + "- if the geoRaster has a nodata value, the nodata value is excluded from min and max. ");
        UnaryFunctionDescriptor.register();
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "extrema";
    }

    /**
     * Fetch the first data value
     * @param im image
     * @param noData nodata
     * @return data value or null if not found
     */
    private static Double fetchDataValue(RenderedImage im, double noData) {
        for(int yTile = im.getMinTileY(); yTile < im.getNumYTiles() - im.getMinTileY() ;
            yTile++) {
            for(int xTile = im.getMinTileX(); xTile < im.getNumXTiles() - im.getMinTileX
                    (); xTile++) {
                Raster tile  = im.getTile(xTile, yTile);
                double[] samples = new double[tile.getWidth()];
                for(int y = tile.getMinY(); y < tile.getHeight() - tile.getMinY(); y++) {
                    tile.getSamples(0, y, tile.getWidth(), 1, 0, samples);
                    for(double sample : samples) {
                        if(Double.compare(sample, noData) != 0) {
                            return sample;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param geoRaster
     * @return
     * @throws IOException
     * @throws SQLException 
     */
    public static double[] extrema(GeoRaster geoRaster) throws IOException, SQLException {
        if (geoRaster == null) {
            return null;
        }
        RasterUtils.RasterMetaData metaData = geoRaster.getMetaData();
        if (geoRaster.getMetaData().numBands != 1) {
            throw new IllegalArgumentException("ST_Extrema accept only raster with one band");
        }

        ParameterBlock pb = new ParameterBlock();
        
        RasterUtils.RasterBandMetaData band = metaData.bands[0];
        
        if (band.hasNoData) {
            // Image has NoData
            // ROI can't be used as it is very slow when using Image as ROI
            // Using RangeFilter we will just replace noData by any non-nodata value
            final double nodataValue = band.noDataValue;
            Double dataValue = fetchDataValue(geoRaster, nodataValue);
            if(dataValue != null) {
                ParameterBlock pbNodata = new ParameterBlock();
                // Constant stick to the image layout of georaster for tile compatibility
                RenderingHints renderingHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout(geoRaster));
                pbNodata.addSource(ConstantDescriptor.create((float) metaData.width, (float) metaData.height,
                        new Double[]{dataValue}, renderingHints));
                pbNodata.addSource(geoRaster);
                pbNodata.add(new double[][]{{nodataValue, nodataValue}});
                pbNodata.add(false); // return dataValue on nodata
                RenderedOp image = JAI.create("RangeFilter", pbNodata, renderingHints);
                pb.addSource(image);
            } else {
                // There is only no-data in this image
                return null;
            }
            // Loop through bands to fetch pixel != nodata
        } else {
            pb.addSource(geoRaster);
        }

        RenderedOp op = JAI.create("extrema", pb);

        // Retrieve both the maximum and minimum pixel value
        double[] allMins = (double[]) op.getProperty("minimum");
        double[] allMaxs = (double[]) op.getProperty("maximum");
        return new double[]{allMins[0], allMaxs[0]};
    }
    
    /**
     * A static class to replace the nodata values by 0 and other values by 1. 
     */
    private static class ReplaceNodata implements UnaryFunction<Double, Double>{
        private final double nodataValue;
        
        public ReplaceNodata(double nodataValue){
            this.nodataValue = nodataValue;
        }
        
        @Override
        public Double invoke(Double arg) {
            if (Double.compare(arg, nodataValue) != 0) {
                return 1d;
            } else {
                return 0d;
            }
        }
    }
    
}
