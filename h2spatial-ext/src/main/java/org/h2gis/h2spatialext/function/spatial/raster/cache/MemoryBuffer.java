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
package org.h2gis.h2spatialext.function.spatial.raster.cache;

import javax.media.jai.PlanarImage;
import java.awt.image.RenderedImage;

/**
 * Image copied in memory
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class MemoryBuffer implements StoredImage {
    private RenderedImage imageCopy;
    public MemoryBuffer(PlanarImage image) {
        imageCopy = image.getAsBufferedImage();
    }

    @Override
    public RenderedImage getImage() {
        return imageCopy;
    }

    @Override
    public void free() {
        imageCopy = null;
    }
}