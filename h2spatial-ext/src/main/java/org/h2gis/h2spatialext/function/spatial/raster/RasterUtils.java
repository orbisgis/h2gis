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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

/**
 *
 * @author Erwan Bocher
 */
public class RasterUtils {
    
    /**
     * Display the color model as an image
     *
     * @param colorModel
     * @return
     */
    public static Image displayColorModel(ColorModel colorModel) {
        final int width = 256;
        final int height = 20;
        int size = width * height;
        byte[] pixels = new byte[size];
        for (int i = 0; i < width * height; i++) {
            pixels[i] = (byte) 255;
        }
        DataBuffer db = new DataBufferByte(pixels, width * height, 0);
        IndexColorModel icm = createDefaultColorModel();
        WritableRaster wr = icm.createCompatibleWritableRaster(1, 1);
        SampleModel sampleModel = wr.getSampleModel();
        sampleModel = sampleModel.createCompatibleSampleModel(width, height);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, db, null);

        if (colorModel instanceof IndexColorModel) {
            return new BufferedImage(colorModel, raster, false, null);
        }
        return new BufferedImage(createDefaultColorModel(), raster, false, null);
    }
    
    /**
     * Create a the default  IndexColorModel.
     *
     * @return
     */
    public static IndexColorModel createDefaultColorModel() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        for (int i = 0; i < 256; i++) {
            r[i] = (byte) i;
            g[i] = (byte) i;
            b[i] = (byte) i;
        }
        return new IndexColorModel(8, 256, r, g, b);
    }
    
    /**
     * Inverts the values in this image's LUT (indexed color model).
     *
     * @param colorModel
     * @return 
     */
    public static ColorModel invertLut(ColorModel colorModel) {
        if (colorModel == null) {
            colorModel = createDefaultColorModel();
        }
        IndexColorModel icm = (IndexColorModel) colorModel;
        int mapSize = icm.getMapSize();
        byte[] reds = new byte[mapSize];
        byte[] greens = new byte[mapSize];
        byte[] blues = new byte[mapSize];
        byte[] reds2 = new byte[mapSize];
        byte[] greens2 = new byte[mapSize];
        byte[] blues2 = new byte[mapSize];
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        for (int i = 0; i < mapSize; i++) {
            reds2[i] = (byte) (reds[mapSize - i - 1] & 255);
            greens2[i] = (byte) (greens[mapSize - i - 1] & 255);
            blues2[i] = (byte) (blues[mapSize - i - 1] & 255);
        }
        return new IndexColorModel(8, mapSize, reds2, greens2, blues2);
    }
    
    /**
         * 
         * @param image
         * @return
         */
        public static PlanarImage byteScale(PlanarImage image) {
                ParameterBlock pbMaxMin = new ParameterBlock();
            pbMaxMin.addSource(image);
            RenderedOp extrem = JAI.create("Extrema", pbMaxMin);
            double[][] extrema = (double[][]) extrem.getProperty("Extrema");

            // Rescale the image with the parameters
            double[] scale = new double[image.getNumBands()];
            double[] offset = new double[image.getNumBands()];
            for (int b=0; b<image.getNumBands(); b++) {
                scale[b] = 255.0 / (extrema[1][b] - extrema[0][b]);
                    offset[b] = (255.0 * extrema[0][b]) / (extrema[0][b] - extrema[1][b]);
            }
            
            ParameterBlockJAI pbRescale = new ParameterBlockJAI("Rescale");
            pbRescale.addSource(image);
            pbRescale.setParameter("constants", scale);
            pbRescale.setParameter("offsets", offset);
            PlanarImage surrogateImage = (PlanarImage)JAI.create("Rescale", pbRescale, null);

            ParameterBlock pbConvert = new ParameterBlock();
            pbConvert.addSource(surrogateImage);
            pbConvert.add(DataBuffer.TYPE_BYTE);
            return JAI.create("format", pbConvert);
        }
        
     /**
     * Adjust to a Uniform distribution CDF.
     * @param source
     * @return the stretched image.
     */
    public static PlanarImage linearStretch(PlanarImage source) {
        // From JAI programming guide
        int numBands = source.getNumBands();
        int binCount = 256;
        // Create an equalization CDF.
        float[][] CDFeq = new float[numBands][];
        for(int b = 0; b < numBands; b++) {
                CDFeq[b] = new float[binCount];
                for(int i = 0; i < binCount; i++) {
                        CDFeq[b][i] = (float)(i+1)/(float)binCount;
                }
        }
        int[] bins = { 256 };
        double[] low = { 0.0D };
        double[] high = { 256.0D };

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(source);
        pb.add(null);
        pb.add(1);
        pb.add(1);
        pb.add(bins);
        pb.add(low);
        pb.add(high);

        RenderedOp fmt = JAI.create("histogram", pb, null);
        // Create a histogram-equalized image.
        return JAI.create("matchcdf", fmt, CDFeq);
    }


}
