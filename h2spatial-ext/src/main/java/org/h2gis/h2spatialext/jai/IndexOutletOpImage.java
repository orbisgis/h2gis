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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Associate a unique index to all outlets
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class IndexOutletOpImage extends Area3x3OpImage {
    private final double noDataValue;
    private final boolean hasNoData;

    private AtomicInteger maxOutletIndex = new AtomicInteger(1);

    public IndexOutletOpImage(RenderedImage flowDirectionSource,boolean hasNoData ,double noData,
            BorderExtender extender, Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(flowDirectionSource, extender, config, layout);
        noDataValue = noData;
        this.hasNoData = hasNoData;
    }

    @Override
    public Object getProperty(String name) {
        return super.getProperty(name);
    }

    @Override
    protected double computeCell(int band, double[][] srcNeighborsValues) {
        final double[] dirValues = srcNeighborsValues[0];
        // If flow of current cell goes somewhere
        if(!hasNoData || Double.compare(dirValues[SRC_INDEX], noDataValue) != 0) {
            // Look for incoming stream
            Integer neighIndex = FlowDirectionRIF.DIRECTIONS_INDEX.get((int)dirValues[SRC_INDEX]);
            if(neighIndex != null && neighIndex != SRC_INDEX &&
                    (!hasNoData || Double.compare(dirValues[neighIndex], noDataValue) != 0)) {
                final double outputCellDir = dirValues[neighIndex];
                // If output cell flow ends (border of image or sink)
                Integer neighDirection = FlowDirectionRIF.DIRECTIONS_INDEX.get((int) outputCellDir);
                if(neighDirection == null || neighDirection.equals(SRC_INDEX)) {
                    // Look at neighbor if stream is incoming
                    for (int idNeigh = 0; idNeigh < dirValues.length; idNeigh++) {
                        // If this is not our cell, and neighbor is not nodata
                        if(idNeigh != SRC_INDEX && (!hasNoData || Double.compare(dirValues[idNeigh], noDataValue)
                                != 0) && dirValues[idNeigh] == FlowAccumulationOpImage.DO_ACCUMULATION[idNeigh]) {
                            // Stream goes in but do not goes out
                            return maxOutletIndex.getAndAdd(1);
                        }
                    }
                }
            }
        }
        return 0; // Not an outlet
    }
}
