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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderConstant;
import javax.media.jai.ImageLayout;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

/**
 * RenderedImageFactory for D8_Watershed
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class IndexPropagationRIF implements RenderedImageFactory {
    private Logger LOGGER = LoggerFactory.getLogger(IndexPropagationRIF.class);

    /**
     * Empty constructor required
     */
    public IndexPropagationRIF() {

    }

    /**
     * The create method, that will be called to create a RenderedImage (or chain
     * of operators that represents one).
     */
    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints)
    {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);

        RenderedImage outletIndexImage = paramBlock.getRenderedSource(0);
        RenderedImage flowDirectionImage = paramBlock.getRenderedSource(1);
        Double noData = Double.NaN;
        boolean hasNoData = false;
        if(paramBlock.getNumParameters() > 0) {
            Double noDataParam = (Double) paramBlock.getObjectParameter(0);
            hasNoData = noDataParam != null;
            if(hasNoData) {
                noData = noDataParam;
            }
        }
        BorderExtender extender = new BorderExtenderConstant(new double[]{0, 0});
        return new IndexPropagationOpImage(outletIndexImage,flowDirectionImage, hasNoData, noData, extender,
                renderHints, layout);
    }
}
