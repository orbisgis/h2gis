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


import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import java.awt.image.RenderedImage;
import java.util.Map;

/**
 * Flow accumulation operation on raster
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class FlowAccumulationOpImage extends Area3x3OpImage {
    private final double[] bandsNoDataValue;
    // Index of raster
    private static final int WEIGHT = 0;
    private static final int DIR = 1;
    // Sum weight where direction goes to central cell
    // Ex: If on right cell the direction is left them sum it
    private static final double[] DO_ACCUMULATION = new double[] {
            FlowDirectionRIF.FLOW_BOTTOM_RIGHT,
            FlowDirectionRIF.FLOW_BOTTOM,
            FlowDirectionRIF.FLOW_BOTTOM_LEFT,
            FlowDirectionRIF.FLOW_RIGHT,
            FlowDirectionRIF.FLOW_NO_DIRECTION,
            FlowDirectionRIF.FLOW_LEFT,
            FlowDirectionRIF.FLOW_TOP_RIGHT,
            FlowDirectionRIF.FLOW_TOP,
            FlowDirectionRIF.FLOW_TOP_LEFT
    };

    public FlowAccumulationOpImage(RenderedImage source, double[] noData, BorderExtender extender,
            Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(source, extender, config, layout);
        bandsNoDataValue = noData;
    }

    @Override
    protected double computeCell(int band, double[][] srcNeighborsValues) {
        final double[] weightValues = srcNeighborsValues[WEIGHT];
        final double[] dirValues = srcNeighborsValues[DIR];
        final double noDataValue = bandsNoDataValue == null ? Double.NaN : bandsNoDataValue[band];
        if(bandsNoDataValue == null || Double.compare(dirValues[SRC_INDEX], noDataValue) != 0) {
            double sum = 0;
            for (int idNeigh = 0; idNeigh < weightValues.length; idNeigh++) {
                if(idNeigh != SRC_INDEX && (bandsNoDataValue == null || Double.compare(dirValues[idNeigh], noDataValue)
                        != 0) && dirValues[idNeigh] == DO_ACCUMULATION[idNeigh]) {
                    sum += weightValues[idNeigh];
                }
            }
            return sum;
        } else {
            return noDataValue;
        }
    }
}
