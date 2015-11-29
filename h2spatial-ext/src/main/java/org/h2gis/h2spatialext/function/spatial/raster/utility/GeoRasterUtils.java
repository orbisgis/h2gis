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

package org.h2gis.h2spatialext.function.spatial.raster.utility;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

/**
 * Some utilities 
 * @author Erwan Bocher
 */
public class GeoRasterUtils {

    
     /**
      * 
     * Returns a rescaled version of the given RenderedImage in the given sample value range.
     * The image returned is always of type 'byte' and has the same number of bands as the source image.
     *
     * @param renderedImage the source georaster, can be of any type
     * @param low   the minimum value of the range
     * @param high  the maximum value of the range
     *
     * @return a rescaled version of the source image
     * @throws java.io.IOException
     * @see : org.esa.beam.util.jai
     * This code is from BEAM (GNU General Public License)
     * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
     */
    public static RenderedImage createRescaledImage(RenderedImage renderedImage,
                                                  double low,
                                                  double high) throws IOException {
        if (renderedImage == null) {
            throw new IllegalArgumentException("[" + renderedImage + "] is null");
        }
        
        ParameterBlock pb = null;
        PlanarImage dst = null;
        int bands = renderedImage.getSampleModel().getNumBands();
        int dtype = renderedImage.getSampleModel().getDataType();
        double slope;
        double y_int;
        if (dtype == DataBuffer.TYPE_BYTE) {
            // use a lookup table for rescaling
            if (high != low) {
                slope = 256.0 / (high - low);
                y_int = 256.0 - slope * high;
            } else {
                slope = 0.0;
                y_int = 0.0;
            }

            // @todo 3 se/nf - (dpc1) duplicated code -> search (dpc2)
            byte[][] lut = new byte[bands][256];
            for (int j = 0; j < bands; j++) {
                byte[] lutb = lut[j];
                for (int i = 0; i < 256; i++) {
                    int value = (int) (slope * i + y_int);
                    if (value < (int) low) {
                        value = 0;
                    } else if (value > (int) high) {
                        value = 255;
                    } else {
                        value &= 0xFF;
                    }
                    lutb[i] = (byte) value;
                }
            }

            LookupTableJAI lookup = new LookupTableJAI(lut);
            pb = new ParameterBlock();
            pb.addSource(renderedImage);
            pb.add(lookup);
            dst = JAI.create("lookup", pb, null);

        } else if (dtype == DataBuffer.TYPE_SHORT
                   || dtype == DataBuffer.TYPE_USHORT) {

            // use a lookup table for rescaling
            if (high != low) {
                slope = 256.0 / (high - low);
                y_int = 256.0 - slope * high;
            } else {
                slope = 0.0;
                y_int = 0.0;
            }

            // @todo 3 se/nf - (dpc2) duplicated code -> search (dpc1)
            byte[][] lut = new byte[bands][65536];
            for (int j = 0; j < bands; j++) {
                byte[] lutb = lut[j];
                for (int i = 0; i < 65535; i++) {
                    int value = (int) (slope * i + y_int);
                    if (dtype == DataBuffer.TYPE_USHORT) {
                        value &= 0xFFFF;
                    }
                    if (value < (int) low) {
                        value = 0;
                    } else if (value > (int) high) {
                        value = 255;
                    } else {
                        value &= 0xFF;
                    }
                    lutb[i] = (byte) value;
                }
            }

            LookupTableJAI lookup = new LookupTableJAI(lut);

            pb = new ParameterBlock();
            pb.addSource(renderedImage);
            pb.add(lookup);
            dst = JAI.create("lookup", pb, null);

        } else if (dtype == DataBuffer.TYPE_INT
                   || dtype == DataBuffer.TYPE_FLOAT
                   || dtype == DataBuffer.TYPE_DOUBLE) {

            // use the rescale and format ops
            if (high != low) {
                slope = 256.0 / (high - low);
                y_int = 256.0 - slope * high;
            } else {
                slope = 0.0;
                y_int = 0.0;
            }

            dst = createRescaleOp(renderedImage, slope, y_int);

            // produce a byte image
            pb = new ParameterBlock();
            pb.addSource(dst);
            pb.add(DataBuffer.TYPE_BYTE);
            dst = JAI.create("format", pb, null);
        }
        return dst;
    }
    
    /**
     * Apply a rescale operator on the RenderedImage
     * @param src
     * @param scale
     * @param offset
     * @return 
     */
    private static RenderedOp createRescaleOp(RenderedImage renderedImage,
            double scale,
            double offset) {
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(renderedImage);
        pb.add(new double[]{scale});
        pb.add(new double[]{offset});
        return JAI.create("rescale", pb, null);
    }
}
