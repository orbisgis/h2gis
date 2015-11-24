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
 * Propagate outlet index in raster according to reverse flow direction
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class IndexPropagationOpImage extends Area3x3OpImage {
    private final double noDataValue;
    private final boolean hasNoData;
    public static final String PROPERTY_EFFECTIVE_INDEX_COPY = "effectiveIndexCopy";

    private AtomicBoolean effectiveIndexCopy = new AtomicBoolean(false);
    // Index of raster
    private static final int INDEX = 0;
    private static final int DIR = 1;

    public IndexPropagationOpImage(RenderedImage weightSource, RenderedImage flowDirectionSource, boolean hasNoData,
            double noData, BorderExtender extender, Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(Arrays.asList(weightSource, flowDirectionSource), extender, config, layout);
        this.hasNoData = hasNoData;
        this.noDataValue = noData;
        properties.setProperty(PROPERTY_EFFECTIVE_INDEX_COPY, effectiveIndexCopy);
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
                if(Double.compare(source.getSampleDouble(x, y, INDEX), 0.d) != 0) {
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
        final double[] outletIndexValues = srcNeighborsValues[INDEX];
        final double[] dirValues = srcNeighborsValues[DIR];
        // Get neighbor index following direction of current cell
        Integer neighIndex = FlowDirectionRIF.DIRECTIONS_INDEX.get((int)dirValues[SRC_INDEX]);
        // If neigh direction is not nodata
        if(neighIndex != null && neighIndex != SRC_INDEX &&
                (!hasNoData || Double.compare(dirValues[neighIndex], noDataValue) != 0)) {
            final double outletIndex = outletIndexValues[neighIndex];
            // Check if we copy nothing or an actual outlet
            if(Double.compare(outletIndex, 0) != 0) {
                effectiveIndexCopy.set(true);
                return outletIndex;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
