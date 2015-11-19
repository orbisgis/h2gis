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
import java.awt.*;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Flow accumulation operation on raster
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class FlowAccumulationOpImage extends Area3x3OpImage {
    private final double[] bandsNoDataValue;
    public static final String PROPERTY_NON_ZERO_FLOW_ACCUM = "nonZeroFlowAccum";

    private AtomicBoolean nonZeroFlowAccum = new AtomicBoolean(false);
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

    public FlowAccumulationOpImage(RenderedImage weightSource,RenderedImage flowDirectionSource, double[] noData,
            BorderExtender extender,
            Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(Arrays.asList(weightSource, flowDirectionSource), extender, config, layout);
        bandsNoDataValue = noData;
        properties.setProperty(PROPERTY_NON_ZERO_FLOW_ACCUM, nonZeroFlowAccum);
    }

    @Override
    public Object getProperty(String name) {
        return super.getProperty(name);
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
        // If input weight is != 0 then do the computation
        Raster source = sources[0];
        boolean doComputation = false;
        final int minY = destRect.y - 1;
        final int minX = destRect.x - 1;
        final int maxY = destRect.y + destRect.height + 1;
        final int maxX = destRect.x + destRect.width + 1;
        for(int y = minY; y < maxY; y++) {
            for(int x = minX; x < maxX; x++) {
                if(Double.compare(source.getSampleDouble(x, y, WEIGHT), 0.d) != 0) {
                    doComputation = true;
                    break;
                }
            }
        }
        if(doComputation) {
            super.computeRect(sources, dest, destRect);
        }
    }

    @Override
    protected double computeCell(int band, double[][] srcNeighborsValues) {
        final double[] weightValues = srcNeighborsValues[WEIGHT];
        final double[] dirValues = srcNeighborsValues[DIR];
        final double noDataValue = bandsNoDataValue == null ? Double.NaN : bandsNoDataValue[DIR];
        if(bandsNoDataValue == null || Double.compare(dirValues[SRC_INDEX], noDataValue) != 0) {
            double sum = 0;
            for (int idNeigh = 0; idNeigh < weightValues.length; idNeigh++) {
                // If this is not our cell, and neighbor is not nodata
                if(idNeigh != SRC_INDEX && (bandsNoDataValue == null || Double.compare(dirValues[idNeigh], noDataValue)
                        != 0) && dirValues[idNeigh] == DO_ACCUMULATION[idNeigh]) {
                    sum += weightValues[idNeigh];
                }
            }
            if(!nonZeroFlowAccum.get() && Double.compare(sum, 0) != 0) {
                nonZeroFlowAccum.set(true);
            }
            return sum;
        } else {
            return 0;
        }
    }
}
