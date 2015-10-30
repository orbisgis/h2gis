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

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import java.awt.image.RenderedImage;
import java.util.Map;

/**
 * Slope operation on raster
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class SlopeOpImage extends Area3x3OpImage {
    private enum UnitType {PERCENT, DEGREE, RADIANT};
    private final UnitType unit;
    private RasterUtils.RasterMetaData metaData;
    private final double pixelSizeX;
    private final double pixelSizeY;
    private final static double FACTOR = 180 / Math.PI;
    private final static short[] neighboursDirection = new short[] { 5, 6, 7,
            8, 1, 2, 3, 4 };
    public final static float indecisionDirection = -1;
    public final static float indecisionAngle = 0;

    private double[] invD8Distances;
    private double[] d8Distances;

    public SlopeOpImage(RenderedImage source, RasterUtils.RasterMetaData metaData, BorderExtender extender,
            Map config, ImageLayout
            layout, String unitName) {
        // Require 1 neighbors around the source pixel
        super(source, extender, config, layout);
        this.unit = UnitType.valueOf(unitName);
        this.metaData = metaData;
        pixelSizeX = Math.abs(metaData.scaleX);
        pixelSizeY = Math.abs(metaData.scaleY);
    }

    @Override
    protected double computeCell(int i, int j, int band, final double[] neighborsValues) {
        return 0;
    }
}
