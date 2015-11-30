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

import java.awt.image.RenderedImage;
import java.sql.SQLException;

/**
 * Image not stored
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class NoBuffer implements StoredImage {
    private RenderedImage image;

    public NoBuffer(RenderedImage image) {
        this.image = image;
    }

    @Override
    public RenderedImage getImage() throws SQLException {
        return image;
    }

    @Override
    public void free() throws SQLException {
        image = null;
    }
}