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
package org.h2gis.h2spatialext.jai;

import com.sun.media.jai.opimage.RIFUtil;
import org.h2.api.GeoRaster;
import org.h2.util.RasterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderConstant;
import javax.media.jai.ImageLayout;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nicolas Fortin
 */
public class IndexOutletRIF implements RenderedImageFactory {
    private Logger LOGGER = LoggerFactory.getLogger(IndexOutletRIF.class);

    /**
     * Empty constructor required
     */
    public IndexOutletRIF()
    {
    }

    /**
     * The create method, that will be called to create a RenderedImage (or chain
     * of operators that represents one).
     */
    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints)
    {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);

        RenderedImage im = paramBlock.getRenderedSource(0);

        if(!(im instanceof GeoRaster)) {
            LOGGER.error(getClass().getSimpleName()+" require Raster spatial metadata");
            return null;
        }

        GeoRaster geoRaster = (GeoRaster) im;

        try {
            final RasterUtils.RasterMetaData metaData = geoRaster.getMetaData();


            double[] noData = new double[metaData.numBands];
            for(int idBand = 0; idBand < noData.length; idBand++) {
                RasterUtils.RasterBandMetaData bandMetaData = metaData.bands[idBand];
                noData[idBand] = bandMetaData.hasNoData ? bandMetaData.noDataValue : Double.NaN;
            }
            BorderExtender extender = new BorderExtenderConstant(noData);

            return new IndexOutletOpImage(im,noData, extender, renderHints, layout);
        } catch (IOException ex) {
            LOGGER.error("Error while reading metadata", ex);
            return null;
        }
    }
}
