/*
 * Copyright (C) 2015 CNRS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.h2gis.h2spatialext.function.spatial.raster.utility;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

/**
 *
 * @author Erwan Bocher
 */
public class GeoRasterColours {
    
    
    /**
     * Returns the transparent pixel value of the renderedImage, or -1 if none.
     * @param renderedImage
     * @return 
     */
    public final int getTransparentPixel(RenderedImage renderedImage) {
        final ColorModel cm = renderedImage.getColorModel();
        return (cm instanceof IndexColorModel) ? ((IndexColorModel) cm)
                .getTransparentPixel() : -1;
    }
    
}
