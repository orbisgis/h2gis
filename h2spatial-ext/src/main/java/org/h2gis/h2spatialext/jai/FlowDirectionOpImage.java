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

import org.h2.util.RasterUtils;

import javax.media.jai.ImageLayout;
import java.awt.image.RenderedImage;
import java.util.Map;

/**
 * Flow direction operation on raster
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class FlowDirectionOpImage extends SlopeOpImage {

    public FlowDirectionOpImage(RenderedImage source, RasterUtils.RasterMetaData metaData,
            Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(source, metaData, null, config, layout, "PERCENT");
    }

    @Override
    protected double computeCell(int band, double[][] srcNeighborsValues) {
        // Take account of only one source image
        final double[] neighborsValues = srcNeighborsValues[0];
        final double noDataValue = metaData.bands[band].noDataValue;
        final double cellValue = neighborsValues[SRC_INDEX];
        if(Double.compare(cellValue, noDataValue) != 0) {
            double maxSlope = Double.NEGATIVE_INFINITY;
            int maxSlopeNeigh = -1;
            for (int idNeigh = 0; idNeigh < neighborsValues.length; idNeigh++) {
                if(idNeigh != SRC_INDEX && Double.compare(neighborsValues[idNeigh], noDataValue) != 0) {
                    double heightRatio = (cellValue - neighborsValues[idNeigh]) * invDistanceMatrix[idNeigh];
                    if(heightRatio > maxSlope) {
                        maxSlope = heightRatio;
                        maxSlopeNeigh = idNeigh;
                    }
                }
            }
            if(maxSlopeNeigh != -1) {
                if(Double.compare(neighborsValues[maxSlopeNeigh], cellValue) == 0) {
                    // Steepest slope is same level
                    return FlowDirectionRIF.FLOW_NO_DIRECTION;
                } else if(maxSlope < Double.MIN_VALUE) {
                    // Steepest slope is negative
                    return FlowDirectionRIF.FLOW_SINK;
                } else {
                    // Flow is going downward, ok
                    return FlowDirectionRIF.DIRECTIONS[maxSlopeNeigh];
                }
            } else {
                // Surrounded by no data
                return FlowDirectionRIF.FLOW_NO_DIRECTION;
            }
        } else {
            return metaData.bands[band].noDataValue;
        }
    }
}
